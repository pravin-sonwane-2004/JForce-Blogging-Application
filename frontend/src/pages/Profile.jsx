import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { postApi } from '../services/postService';
import Pagination from '../components/Pagination';

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const load = async (p) => {
    setLoading(true);
    setError('');
    try {
      const data = await postApi.mine({ page: p, size: 6 });
      setPosts(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(page);
  }, [page]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this post?')) return;
    try {
      await postApi.remove(id);
      if (posts.length === 1 && page > 0) {
        setPage(page - 1);
      } else {
        load(page);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <h1>My Profile</h1>

      <div className="card profile-card">
        <div className="profile-info">
          <p><strong>Username:</strong> {user.username}</p>
          <p><strong>Email:</strong> {user.email}</p>
          <p>
            <strong>Role:</strong>{' '}
            <span className={`badge badge-${user.role.toLowerCase()}`}>{user.role}</span>
          </p>
          <p><strong>Member since:</strong> {new Date(user.createdAt).toLocaleDateString()}</p>
          <button className="btn btn-secondary" onClick={handleLogout}>Logout</button>
        </div>
      </div>

      <h2>My Posts</h2>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="empty">Loading...</p>
      ) : posts.length === 0 ? (
        <p className="empty">You have not written any posts yet.</p>
      ) : (
        <>
          <div className="card">
            {posts.map((p) => (
              <div className="profile-post" key={p.id}>
                <div className="profile-post-main">
                  <Link to={`/posts/${p.id}`}>{p.title}</Link>
                  <span className={`badge badge-${p.status.toLowerCase()}`}>{p.status}</span>
                </div>
                <span className="profile-post-date">{new Date(p.createdAt).toLocaleDateString()}</span>
                <button className="btn btn-small btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
              </div>
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </section>
  );
}