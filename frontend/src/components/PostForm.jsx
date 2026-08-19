import { useState } from 'react';

export default function PostForm({ initial, onSubmit, onCancel }) {
  const [title, setTitle] = useState(initial?.title || '');
  const [content, setContent] = useState(initial?.content || '');
  const [tags, setTags] = useState(initial?.tags || '');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await onSubmit({ title, content, tags });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="post-form card" onSubmit={handleSubmit}>
      <h3>{initial ? 'Edit post' : 'Write a new post'}</h3>

      {error && <div className="alert alert-error">{error}</div>}

      <input
        className="input"
        placeholder="Title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        maxLength={150}
        required
      />
      <textarea
        className="input"
        placeholder="Write your content..."
        rows={6}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        required
      />
      <input
        className="input"
        placeholder="Tags (comma separated, e.g. java, spring)"
        value={tags}
        onChange={(e) => setTags(e.target.value)}
      />

      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Saving...' : (initial ? 'Save changes' : 'Publish')}
        </button>
        <button type="button" className="btn btn-secondary" onClick={onCancel}>Cancel</button>
      </div>

      <p className="hint">New posts are sent to moderation and appear publicly after an admin approves them.</p>
    </form>
  );
}