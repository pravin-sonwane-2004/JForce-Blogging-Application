import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../services/adminService';
import { postApi } from '../services/postService';

export default function Admin() {
  const [tab, setTab] = useState('posts');

  return (
    <section>
      <h1>Admin Panel</h1>
      <div className="tabs">
        <button className={`tab-btn ${tab === 'posts' ? 'active' : ''}`} onClick={() => setTab('posts')}>Posts</button>
        <button className={`tab-btn ${tab === 'users' ? 'active' : ''}`} onClick={() => setTab('users')}>Users</button>
        <button className={`tab-btn ${tab === 'reports' ? 'active' : ''}`} onClick={() => setTab('reports')}>Reports</button>
      </div>

      {tab === 'posts' && <PostsTab key="posts" />}
      {tab === 'users' && <UsersTab key="users" />}
      {tab === 'reports' && <ReportsTab key="reports" />}
    </section>
  );
}

function PostsTab() {
  const [posts, setPosts] = useState([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async (filter) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: 0, size: 50 };
      if (filter) params.status = filter;
      const data = await adminApi.allPosts(params);
      setPosts(data.content);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(status);
  }, [status]);

  const run = async (fn) => {
    try {
      await fn();
      load(status);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="card">
      <div className="toolbar">
        <select className="input" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="empty">Loading...</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Author</th>
              <th>Status</th>
              <th>Views</th>
              <th>Featured</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {posts.map((p) => (
              <tr key={p.id}>
                <td><Link to={`/posts/${p.id}`}>{p.title}</Link></td>
                <td>{p.author}</td>
                <td><span className={`badge badge-${p.status.toLowerCase()}`}>{p.status}</span></td>
                <td>{p.views}</td>
                <td>{p.featured ? '★ Yes' : '—'}</td>
                <td className="table-actions">
                  <button className="btn btn-small" onClick={() => run(() => adminApi.changeStatus(p.id, 'APPROVED'))}>Approve</button>
                  <button className="btn btn-small" onClick={() => run(() => adminApi.changeStatus(p.id, 'REJECTED'))}>Reject</button>
                  <button className="btn btn-small" onClick={() => run(() => adminApi.toggleFeatured(p.id))}>
                    {p.featured ? 'Unfeature' : 'Feature'}
                  </button>
                  <button className="btn btn-small btn-danger" onClick={() => run(() => postApi.remove(p.id))}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function UsersTab() {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setUsers(await adminApi.users());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const changeRole = async (id, role) => {
    try {
      await adminApi.updateRole(id, role);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const remove = async (id) => {
    if (!window.confirm('Delete this user and all their posts?')) return;
    try {
      await adminApi.deleteUser(id);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="card">
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="empty">Loading...</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Role</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>
                  <select
                    className="input"
                    value={u.role}
                    onChange={(e) => changeRole(u.id, e.target.value)}
                  >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                <td>
                  <button className="btn btn-small btn-danger" onClick={() => remove(u.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function ReportsTab() {
  const [reports, setReports] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    adminApi.reports()
      .then(setReports)
      .catch((err) => setError(err.message));
  }, []);

  if (error) return <div className="alert alert-error">{error}</div>;
  if (!reports) return <p className="empty">Loading...</p>;

  return (
    <div className="reports">
      <div className="card">
        <h3>Most active users</h3>
        {reports.mostActiveUsers.length === 0 ? (
          <p className="empty">No posts yet.</p>
        ) : (
          <ol>
            {reports.mostActiveUsers.map((u) => (
              <li key={u.username}>{u.username} — {u.postCount} posts</li>
            ))}
          </ol>
        )}
      </div>

      <div className="card">
        <h3>Most viewed posts</h3>
        {reports.mostViewedPosts.length === 0 ? (
          <p className="empty">No posts yet.</p>
        ) : (
          <ol>
            {reports.mostViewedPosts.map((p) => (
              <li key={p.id}>
                {p.title} <span className="muted">by {p.author} · {p.views} views</span>
              </li>
            ))}
          </ol>
        )}
      </div>
    </div>
  );
}