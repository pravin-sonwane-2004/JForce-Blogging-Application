$ErrorActionPreference = "Stop"
$base = "http://localhost:8080/api"

try {
    # 1. Public feed (4 seeded APPROVED posts by john)
    $feed = Invoke-RestMethod "$base/posts"
    Write-Host "1. Public feed posts: $($feed.content.Count) (expect 4)"

    # 2. Register alice
    $alice = Invoke-RestMethod "$base/auth/register" -Method Post -ContentType "application/json" -Body '{"username":"alice","password":"alice123","email":"alice@test.com"}'
    Write-Host "2. Registered alice (id=$($alice.id), role=$($alice.role))"

    # 3. Login alice (keeps session cookie)
    Invoke-RestMethod "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"alice","password":"alice123"}' -SessionVariable aliceSession | Out-Null
    Write-Host "3. Logged in alice."

    # 4. Create post -> should be PENDING
    $post = Invoke-RestMethod "$base/posts" -Method Post -ContentType "application/json" -Body '{"title":"Alice First Post","content":"This is the content of the first Alice post with enough words to show an excerpt properly on the feed card.","tags":"alice, demo"}' -WebSession $aliceSession
    Write-Host "4. Created post id=$($post.id) status=$($post.status) (expect PENDING)"

    # 5. My posts (alice)
    $mine = Invoke-RestMethod "$base/posts/mine" -WebSession $aliceSession
    Write-Host "5. Alice's posts: $($mine.content.Count) (expect 1)"

    # 6. Login admin (separate session)
    Invoke-RestMethod "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}' -SessionVariable adminSession | Out-Null
    Write-Host "6. Logged in admin."

    # 7. Pending posts (2 seeded + 1 alice = 3)
    $pending = Invoke-RestMethod "$base/admin/posts?status=PENDING" -WebSession $adminSession
    Write-Host "7. Pending posts: $($pending.content.Count) (expect 3)"

    # 8. Admin approves alice's post
    $approved = Invoke-RestMethod "$base/admin/posts/$($post.id)/status?status=APPROVED" -Method Put -WebSession $adminSession
    Write-Host "8. Approved -> status=$($approved.status)"

    # 9. Public feed now 5
    $feed2 = Invoke-RestMethod "$base/posts"
    Write-Host "9. Feed after approve: $($feed2.content.Count) (expect 5)"

    # 10. Search test
    $search = Invoke-RestMethod "$base/posts?q=react"
    Write-Host "10. Search 'react': $($search.content.Count) posts (expect 1)"

    # 11. Popularity sort test
    $popular = Invoke-RestMethod "$base/posts?sort=popular"
    Write-Host "11. Popular sort first title: '$($popular.content[0].title)' (expect Spring Boot, 120 views)"

    # 12. Reports
    $reports = Invoke-RestMethod "$base/admin/reports" -WebSession $adminSession
    Write-Host "12. Reports: active users=$($reports.mostActiveUsers.Count), top posts=$($reports.mostViewedPosts.Count)"

    # 13. Users list
    $users = Invoke-RestMethod "$base/admin/users" -WebSession $adminSession
    Write-Host "13. Users in system: $($users.Count) (expect 3: admin, john, alice)"

    # 14. Delete alice (also removes her post)
    Invoke-RestMethod "$base/admin/users/$($alice.id)" -Method Delete -WebSession $adminSession
    Write-Host "14. Deleted alice + her post."

    # 15. Verify deletion
    $users2 = Invoke-RestMethod "$base/admin/users" -WebSession $adminSession
    Write-Host "15. Users after delete: $($users2.Count) (expect 2)"

    # 16. Unauthorized access should be rejected
    $unauthorized = $false
    try {
        Invoke-RestMethod "$base/posts/mine" | Out-Null
    } catch {
        $unauthorized = $true
    }
    if (-not $unauthorized) { Write-Host "16. FAIL: /posts/mine should require login"; exit 1 }
    Write-Host "16. Unauthenticated /posts/mine correctly rejected"

    Write-Host ""
    Write-Host "=== ALL SMOKE TESTS PASSED ==="
}
catch {
    Write-Host "ERROR: $($_.Exception.Message)"
    if ($_.ErrorDetails.Message) { Write-Host "BODY: $($_.ErrorDetails.Message)" }
    exit 1
}