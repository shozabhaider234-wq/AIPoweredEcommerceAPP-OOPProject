import React, { useState, useEffect, useCallback } from 'react';
import { Search, SlidersHorizontal, X } from 'lucide-react';
import { productAPI, categoryAPI, wishlistAPI } from '../services/api';
import ProductCard from '../components/common/ProductCard';
import { Spinner, EmptyState, Pagination } from '../components/common/UI';
import { useAuth } from '../context/AuthContext';
import { Package } from 'lucide-react';

export default function ShopPage() {
  const { isCustomer } = useAuth();
  const [products, setProducts]   = useState([]);
  const [categories, setCategories] = useState([]);
  const [wishlistIds, setWishlistIds] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [page, setPage]           = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showFilters, setShowFilters] = useState(false);

  const [filters, setFilters] = useState({
    name: '', categoryId: '', minPrice: '', maxPrice: '', sortBy: 'name',
  });
  const [applied, setApplied] = useState(filters);

  useEffect(() => {
    categoryAPI.list().then(r => setCategories(r.data.data || [])).catch(() => {});
    if (isCustomer) wishlistAPI.get().then(r => setWishlistIds(r.data.data?.map(p => p.id) || [])).catch(() => {});
  }, [isCustomer]);

  const loadProducts = useCallback(async (pg = 0, f = applied) => {
    setLoading(true);
    try {
      const hasFilter = f.name || f.categoryId || f.minPrice || f.maxPrice;
      let res;
      if (hasFilter) {
        res = await productAPI.search({ ...f, page: pg, size: 12 });
      } else {
        res = await productAPI.list(pg, 12, f.sortBy);
      }
      const d = res.data.data;
      setProducts(d.content || []);
      setTotalPages(d.totalPages || 1);
      setPage(d.page ?? pg);
    } catch { setProducts([]); }
    finally { setLoading(false); }
  }, [applied]);

  useEffect(() => { loadProducts(0, applied); }, [applied]);

  const handleSearch = (e) => {
    e.preventDefault();
    setApplied({ ...filters });
  };

  const clearFilters = () => {
    const clean = { name: '', categoryId: '', minPrice: '', maxPrice: '', sortBy: 'name' };
    setFilters(clean);
    setApplied(clean);
  };

  const hasActiveFilters = applied.name || applied.categoryId || applied.minPrice || applied.maxPrice;

  return (
    <div className="container page-content">
      {/* Header */}
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ marginBottom: 4 }}>Shop</h1>
        <p className="text-muted">Discover products from top sellers</p>
      </div>

      {/* Search + filters bar */}
      <form onSubmit={handleSearch} style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: '1 1 280px' }}>
            <Search size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--ink-muted)' }} />
            <input
              className="form-input"
              placeholder="Search products…"
              style={{ paddingLeft: 38 }}
              value={filters.name}
              onChange={e => setFilters(f => ({ ...f, name: e.target.value }))}
            />
          </div>

          <select className="form-input" style={{ width: 160 }}
            value={filters.categoryId} onChange={e => setFilters(f => ({ ...f, categoryId: e.target.value }))}>
            <option value="">All categories</option>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>

          <button type="button" className="btn btn-outline" onClick={() => setShowFilters(s => !s)}>
            <SlidersHorizontal size={16} />
            Filters {hasActiveFilters && <span className="badge badge-accent" style={{ padding: '1px 6px' }}>!</span>}
          </button>

          <button type="submit" className="btn btn-primary">
            <Search size={16} /> Search
          </button>

          {hasActiveFilters && (
            <button type="button" className="btn btn-ghost" onClick={clearFilters}>
              <X size={16} /> Clear
            </button>
          )}
        </div>

        {showFilters && (
          <div style={{
            marginTop: 12, padding: '16px 20px',
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-md)',
            display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-end',
          }}>
            <div>
              <label className="form-label">Min price ($)</label>
              <input type="number" className="form-input" style={{ width: 120 }}
                placeholder="0" value={filters.minPrice}
                onChange={e => setFilters(f => ({ ...f, minPrice: e.target.value }))} />
            </div>
            <div>
              <label className="form-label">Max price ($)</label>
              <input type="number" className="form-input" style={{ width: 120 }}
                placeholder="999" value={filters.maxPrice}
                onChange={e => setFilters(f => ({ ...f, maxPrice: e.target.value }))} />
            </div>
            <div>
              <label className="form-label">Sort by</label>
              <select className="form-input" style={{ width: 150 }}
                value={filters.sortBy} onChange={e => setFilters(f => ({ ...f, sortBy: e.target.value }))}>
                <option value="name">Name</option>
                <option value="price">Price (low)</option>
              </select>
            </div>
          </div>
        )}
      </form>

      {/* Products grid */}
      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}>
          <Spinner size={32} />
        </div>
      ) : products.length === 0 ? (
        <EmptyState icon={Package} title="No products found" description="Try adjusting your search or filters." />
      ) : (
        <>
          <p className="text-muted text-small" style={{ marginBottom: 16 }}>
            Showing {products.length} product{products.length !== 1 ? 's' : ''}
          </p>
          <div className="product-grid">
            {products.map(p => (
              <ProductCard
                key={p.id}
                product={p}
                wishlistIds={wishlistIds}
                onWishlistChange={() => wishlistAPI.get().then(r => setWishlistIds(r.data.data?.map(x => x.id) || []))}
              />
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onChange={pg => loadProducts(pg)} />
        </>
      )}
    </div>
  );
}
