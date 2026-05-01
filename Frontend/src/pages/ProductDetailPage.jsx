import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ShoppingCart, Heart, ChevronLeft, Package, Star } from 'lucide-react';
import { productAPI, reviewAPI, wishlistAPI } from '../services/api';
import { Spinner, StarRating, Pagination, InfoBar } from '../components/common/UI';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import toast from 'react-hot-toast';

export default function ProductDetailPage() {
  const { id } = useParams();
  const { user, isCustomer } = useAuth();
  const cart = useCart();

  const [product, setProduct]   = useState(null);
  const [reviews, setReviews]   = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [revPage, setRevPage]   = useState(0);
  const [loading, setLoading]   = useState(true);
  const [qty, setQty]           = useState(1);
  const [addingCart, setAddingCart] = useState(false);
  const [inWishlist, setInWishlist] = useState(false);
  const [selectedImg, setSelectedImg] = useState(0);

  // Review form
  const [revForm, setRevForm]   = useState({ rating: 0, comment: '' });
  const [submittingRev, setSubmittingRev] = useState(false);

  useEffect(() => {
    setLoading(true);
    productAPI.getById(id, user?.id)
      .then(r => { setProduct(r.data.data); setLoading(false); })
      .catch(() => setLoading(false));

    if (isCustomer) {
      wishlistAPI.get().then(r => {
        setInWishlist(r.data.data?.some(p => p.id === Number(id)));
      }).catch(() => {});
    }
  }, [id, user?.id, isCustomer]);

  useEffect(() => {
    reviewAPI.list(id, revPage)
      .then(r => {
        setReviews(r.data.data?.content || []);
        setTotalPages(r.data.data?.totalPages || 1);
      }).catch(() => {});
  }, [id, revPage]);

  const addToCart = async () => {
    if (!cart) return;
    try {
      setAddingCart(true);
      await cart.addItem(product.id, qty);
      toast.success(`${qty} × ${product.name} added to cart`);
    } catch (err) { toast.error(err.userMessage || 'Failed'); }
    finally { setAddingCart(false); }
  };

  const toggleWishlist = async () => {
    try {
      if (inWishlist) { await wishlistAPI.remove(id); toast.success('Removed from wishlist'); }
      else            { await wishlistAPI.add(id);    toast.success('Saved to wishlist'); }
      setInWishlist(w => !w);
    } catch (err) { toast.error(err.userMessage || 'Error'); }
  };

  const submitReview = async (e) => {
    e.preventDefault();
    if (!revForm.rating) { toast.error('Please select a rating'); return; }
    try {
      setSubmittingRev(true);
      await reviewAPI.add(id, revForm);
      toast.success('Review submitted!');
      setRevForm({ rating: 0, comment: '' });
      reviewAPI.list(id, 0).then(r => {
        setReviews(r.data.data?.content || []);
        setTotalPages(r.data.data?.totalPages || 1);
        setRevPage(0);
      });
    } catch (err) { toast.error(err.userMessage || 'Failed'); }
    finally { setSubmittingRev(false); }
  };

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><Spinner size={32} /></div>;
  if (!product) return <div className="container page-content"><InfoBar type="error">Product not found.</InfoBar></div>;

  const images = product.imageUrls?.length ? product.imageUrls : [];
  const mainImg = images[selectedImg] ? `http://localhost:8080${images[selectedImg]}` : null;

  return (
    <div className="container page-content">
      <Link to="/shop" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 14, color: 'var(--ink-muted)', marginBottom: 24 }}>
        <ChevronLeft size={16} /> Back to shop
      </Link>

      {/* Main product section */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 40, marginBottom: 48 }}>
        {/* Images */}
        <div>
          <div style={{
            aspectRatio: '1',
            background: 'var(--surface-3)',
            borderRadius: 'var(--radius-xl)',
            overflow: 'hidden',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            marginBottom: 12,
          }}>
            {mainImg ? <img src={mainImg} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              : <Package size={80} style={{ opacity: 0.15 }} />}
          </div>
          {images.length > 1 && (
            <div style={{ display: 'flex', gap: 10 }}>
              {images.map((url, i) => (
                <button key={i} onClick={() => setSelectedImg(i)} style={{
                  width: 72, height: 72, borderRadius: 'var(--radius-md)',
                  overflow: 'hidden', border: `2px solid ${i === selectedImg ? 'var(--ink)' : 'var(--border)'}`,
                  padding: 0, background: 'var(--surface-3)', cursor: 'pointer',
                }}>
                  <img src={`http://localhost:8080${url}`} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Info */}
        <div>
          <p style={{ fontSize: 12, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--accent)', marginBottom: 8 }}>
            {product.categoryName || 'General'}
          </p>
          <h1 style={{ fontSize: 28, marginBottom: 16 }}>{product.name}</h1>

          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
            <span style={{ fontSize: 32, fontFamily: 'var(--font-display)', fontWeight: 800 }}>
              ${Number(product.price).toFixed(2)}
            </span>
            {product.stock === 0
              ? <span className="badge badge-cancelled">Out of stock</span>
              : <span className="badge badge-delivered">{product.stock} in stock</span>}
          </div>

          {product.sellerStoreName && (
            <p style={{ fontSize: 14, color: 'var(--ink-muted)', marginBottom: 16 }}>
              Sold by <strong style={{ color: 'var(--ink)' }}>{product.sellerStoreName}</strong>
            </p>
          )}

          {product.description && (
            <p style={{ color: 'var(--ink-muted)', lineHeight: 1.7, marginBottom: 24 }}>
              {product.description}
            </p>
          )}

          {isCustomer && product.stock > 0 && (
            <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 16 }}>
              <div style={{
                display: 'flex', alignItems: 'center',
                border: '1.5px solid var(--border)',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
              }}>
                <button className="btn btn-ghost" style={{ padding: '8px 14px', borderRadius: 0 }}
                  onClick={() => setQty(q => Math.max(1, q - 1))}>−</button>
                <span style={{ padding: '8px 16px', fontFamily: 'var(--font-display)', fontWeight: 700, minWidth: 40, textAlign: 'center' }}>
                  {qty}
                </span>
                <button className="btn btn-ghost" style={{ padding: '8px 14px', borderRadius: 0 }}
                  onClick={() => setQty(q => Math.min(product.stock, q + 1))}>+</button>
              </div>
              <button className="btn btn-accent btn-lg" style={{ flex: 1 }} onClick={addToCart} disabled={addingCart}>
                {addingCart ? <Spinner white size={18} /> : <ShoppingCart size={18} />}
                {addingCart ? 'Adding…' : 'Add to cart'}
              </button>
              <button className="btn btn-outline" style={{ padding: '12px 16px' }} onClick={toggleWishlist}>
                <Heart size={18} fill={inWishlist ? 'var(--accent)' : 'none'} color={inWishlist ? 'var(--accent)' : 'currentColor'} />
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Reviews section */}
      <div className="card">
        <div className="card-header">
          <h2 className="card-title">Customer Reviews</h2>
          <span className="text-muted text-small">{reviews.length} review{reviews.length !== 1 ? 's' : ''}</span>
        </div>

        {isCustomer && (
          <div style={{ padding: '20px 24px', borderBottom: '1px solid var(--border)' }}>
            <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: 12 }}>Write a review</p>
            <form onSubmit={submitReview}>
              <div style={{ marginBottom: 12 }}>
                <label className="form-label" style={{ display: 'block', marginBottom: 6 }}>Your rating</label>
                <StarRating value={revForm.rating} size={24} onRate={r => setRevForm(f => ({ ...f, rating: r }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Comment (optional)</label>
                <textarea className="form-input" placeholder="Share your experience…"
                  value={revForm.comment} onChange={e => setRevForm(f => ({ ...f, comment: e.target.value }))} />
              </div>
              <button type="submit" className="btn btn-primary" disabled={submittingRev}>
                {submittingRev ? <Spinner white size={16} /> : null} Submit Review
              </button>
            </form>
          </div>
        )}

        <div className="card-body">
          {reviews.length === 0 ? (
            <p className="text-muted text-small">No reviews yet. Be the first!</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
              {reviews.map(r => (
                <div key={r.id} style={{ paddingBottom: 20, borderBottom: '1px solid var(--border)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                    <div>
                      <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 14 }}>{r.userName}</span>
                    </div>
                    <StarRating value={r.rating} size={14} />
                  </div>
                  {r.comment && <p style={{ fontSize: 14, color: 'var(--ink-muted)', lineHeight: 1.6 }}>{r.comment}</p>}
                </div>
              ))}
            </div>
          )}
          <Pagination page={revPage} totalPages={totalPages} onChange={setRevPage} />
        </div>
      </div>
    </div>
  );
}
