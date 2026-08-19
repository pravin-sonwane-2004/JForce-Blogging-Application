import { useCallback, useEffect, useState } from 'react';
import { postApi } from '../services/postService';
import { useAuth } from '../context/AuthContext';
import PostCard from '../components/PostCard';
import PostForm from '../components/PostForm';
import Pagination from '../components/Pagination';

export default function Home() {
  const { user } = useAuth();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [query, setQuery] = useState('');
  const [q, setQ] = useState('');
  const [sort, setSort] = useState('newest');
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);

  const loadFeed = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await postApi.list({ page, size: 6, q, sort });
      setPosts(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [page, q, sort]);

  useEffect(() => {
    loadFeed();
  }, [loadFeed]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setQ(query.trim());
  };

  const handleClearSearch = () => {
    setQuery('');
    setQ('');
    setPage(0);
  };

  const handleSave = async (payload) => {
    if (editing) {
      await postApi.update(editing.id, payload);
    } else {
      await postApi.create(payload);
    }
    setShowForm(false);
    setEditing(null);
    loadFeed();
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this post?')) return;
    try {
      await postApi.remove(id);
      loadFeed();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <section>
      <div className="home-head">
        <h1>Latest Posts</h1>
        {user && !showForm && (
          <button className="btn btn-primary" onClick={() => { setEditing(null); setShowForm(true); }}>
            + New Post
          </button>
        )}
      </div>

      <form className="toolbar" onSubmit={handleSearch}>
        <input
          className="search-input"
          placeholder="Search by title, author or keywords..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select
          className="input"
          value={sort}
          onChange={(e) => { setSort(e.target.value); setPage(0); }}
        >
          <option value="newest">Sort: Newest</option>
          <option value="popular">Sort: Most popular</option>
          <option value="author">Sort: By author</option>
        </select>
        <button className="btn btn-primary" type="submit">Search</button>
        {q && <button className="btn btn-secondary" type="button" onClick={handleClearSearch}>Clear</button>}
      </form>

      {showForm && (
        <PostForm
          initial={editing}
          onCancel={() => { setShowForm(false); setEditing(null); }}
          onSubmit={handleSave}
        />
      )}

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="empty">Loading posts...</p>
      ) : posts.length === 0 ? (
        <p className="empty">No posts found.</p>
      ) : (
        <>
          <div className="post-grid">
            {posts.map((p) => (
              <PostCard
                key={p.id}
                post={p}
                canManage={!!user && p.author === user.username}
                onEdit={(post) => { setEditing(post); setShowForm(true); }}
                onDelete={handleDelete}
              />
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </section>
  );
}