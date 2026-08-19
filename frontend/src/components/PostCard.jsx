import { Link } from 'react-router-dom';

export default function PostCard({ post, canManage, onEdit, onDelete }) {
  return (
    <article className="post-card">
      <div className="post-card-head">
        {post.featured && <span className="badge badge-featured">Featured</span>}
        {post.status !== 'APPROVED' && (
          <span className={`badge badge-${post.status.toLowerCase()}`}>{post.status}</span>
        )}
      </div>

      <h2 className="post-title">
        <Link to={`/posts/${post.id}`}>{post.title}</Link>
      </h2>
      <p className="post-excerpt">{post.excerpt}</p>

      {post.tags && (
        <div className="tags">
          {post.tags.split(',').map((t) => (
            <span key={t} className="tag">{t.trim()}</span>
          ))}
        </div>
      )}

      <div className="post-meta">
        <span>By <strong>{post.author}</strong></span>
        <span>{new Date(post.createdAt).toLocaleDateString()}</span>
        <span>👁 {post.views} views</span>
      </div>

      {canManage && (
        <div className="post-actions">
          <button className="btn btn-small" onClick={() => onEdit(post)}>Edit</button>
          <button className="btn btn-small btn-danger" onClick={() => onDelete(post.id)}>Delete</button>
        </div>
      )}
    </article>
  );
}