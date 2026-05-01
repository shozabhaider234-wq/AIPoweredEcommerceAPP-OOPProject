import React from 'react';
import { Link } from 'react-router-dom';
import { Heart, ShoppingCart, Package } from 'lucide-react';
import { StarRating } from './UI';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { wishlistAPI } from '../../services/api';
import toast from 'react-hot-toast';

export default function ProductCard({ product, wishlistIds = [], onWishlistChange }) {
  const { isCustomer } = useAuth();
  const cart = useCart();
  const [adding, setAdding] = React.useState(false);
  const [wishlisting, setWishlisting] = React.useState(false);
  const inWishlist = wishlistIds.includes(product.id);

  const img = product.imageUrls?.[0]
    ? `http://localhost:8080${product.imageUrls[0]}`
    : null;

  const handleAddToCart = async (e) => {
    e.preventDefault();
    if (!cart) return;
    try {
      setAdding(true);
      await cart.addItem(product.id, 1);
      toast.success('Added to cart');
    } catch (err) {
      toast.error(err.userMessage || 'Failed to add');
    } finally {
      setAdding(false);
    }
  };

  const handleWishlist = async (e) => {
    e.preventDefault();
    try {
      setWishlisting(true);
      if (inWishlist) {
        await wishlistAPI.remove(product.id);
        toast.success('Removed from wishlist');
      } else {
        await wishlistAPI.add(product.id);
        toast.success('Added to wishlist');
      }
      onWishlistChange?.();
    } catch (err) {
      toast.error(err.userMessage || 'Error');
    } finally {
      setWishlisting(false);
    }
  };

  return (
    <Link to={`/product/${product.id}`} style={{ display: 'block' }}>
      <div className="product-card">
        <div className="product-card-img">
          {img ? (
            <img src={img} alt={product.name} />
          ) : (
            <Package size={48} style={{ opacity: 0.2 }} />
          )}
        </div>

        <div className="product-card-body">
          <p className="product-card-cat">{product.categoryName || 'General'}</p>
          <p className="product-card-name">{product.name}</p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <StarRating value={0} size={13} />
            <span className="text-small text-muted">({product.sellerStoreName || '—'})</span>
          </div>
          <p className="product-card-price">${Number(product.price).toFixed(2)}</p>
          {product.stock === 0 && (
            <span className="badge badge-default" style={{ width: 'fit-content' }}>Out of stock</span>
          )}
        </div>

        {isCustomer && (
          <div className="product-card-footer">
            <button
              className="btn btn-primary btn-sm"
              style={{ flex: 1 }}
              onClick={handleAddToCart}
              disabled={adding || product.stock === 0}
            >
              <ShoppingCart size={14} />
              {adding ? 'Adding…' : 'Add to cart'}
            </button>
            <button
              className="btn btn-ghost btn-sm"
              style={{ padding: '6px 10px' }}
              onClick={handleWishlist}
              disabled={wishlisting}
              title={inWishlist ? 'Remove from wishlist' : 'Save to wishlist'}
            >
              <Heart size={16} fill={inWishlist ? 'var(--accent)' : 'none'} color={inWishlist ? 'var(--accent)' : 'var(--ink-muted)'} />
            </button>
          </div>
        )}
      </div>
    </Link>
  );
}
