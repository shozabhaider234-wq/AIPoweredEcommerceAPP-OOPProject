import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Heart } from 'lucide-react';
import { wishlistAPI } from '../services/api';
import ProductCard from '../components/common/ProductCard';
import { Spinner } from '../components/common/UI';

export default function WishlistPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading]   = useState(true);

  const load = () => {
    wishlistAPI.get()
      .then(r => setProducts(r.data.data || []))
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const wishlistIds = products.map(p => p.id);

  return (
    <div className="container page-content">
      <h1 style={{ marginBottom: 8 }}>Wishlist</h1>
      <p className="text-muted" style={{ marginBottom: 28 }}>Products you've saved for later</p>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}><Spinner size={32} /></div>
      ) : products.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '64px 0' }}>
          <Heart size={64} style={{ margin: '0 auto 20px', opacity: 0.15 }} />
          <h2 style={{ marginBottom: 8 }}>Your wishlist is empty</h2>
          <p className="text-muted" style={{ marginBottom: 24 }}>Tap the heart icon on any product to save it here.</p>
          <Link to="/shop"><button className="btn btn-primary">Browse products</button></Link>
        </div>
      ) : (
        <div className="product-grid">
          {products.map(p => (
            <ProductCard key={p.id} product={p} wishlistIds={wishlistIds} onWishlistChange={load} />
          ))}
        </div>
      )}
    </div>
  );
}
