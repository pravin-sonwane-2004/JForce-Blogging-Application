import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { postApi } from '../services/postService';
import { useAuth } from '../context/AuthContext';
import PostForm from '../components/PostForm';

export default function PostDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    setLoading(true);
    postApi.get(id)
      .then(setPost)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSave = async (payload) => {
    const updated = await postApi.update(id, payload);
    setPost(updated);
    setEditing(false);
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this post?')) return;
    try {
      await postApi.remove(id);
      navigate('/');
    } catch (err) {
      setError(err.message);
    }
  };

  if (loading) return <p className="empty">Loading post...</p>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!post) return null;

  const canManage = !!user && post.author === user.username;

  return (
    <article className="card post-detail">
      <div className="post-card-head">
        {post.featured && <span className="badge badge-featured">Featured</span>}
        {post.status !== 'APPROVED' && (
          <span className={`badge badge-${post.status.toLowerCase()}`}>{post.status}</span>
        )}
      </div>

      {editing ? (
        <PostForm
          initial={post}
          onCancel={() => setEditing(false)}
          onSubmit={handleSave}
        />
      ) : (
        <>
          <h1 className="post-title">{post.title}</h1>
          <div className="post-meta">
            <span>By <strong>{post.author}</strong></span>
            <span>{new Date(post.createdAt).toLocaleDateString()}</span>
            <span>👁 {post.views} views</span>
          </div>
          {post.tags && (
            <div className="tags">
              {post.tags.split(',').map((t) => (
                <span key={t} className="tag">{t.trim()}</span>
              ))}
            </div>
          )}
          <div className="post-content">{post.content}</div>

          {canManage && (
            <div className="post-actions">
              <button className="btn btn-small" onClick={() => setEditing(true)}>Edit</button>
              <button className="btn btn-small btn-danger" onClick={handleDelete}>Delete</button>
            </div>
          )}
        </>
      )}

      <Link to="/" className="back-link">← Back to all posts</Link>
    </article>
  );
}