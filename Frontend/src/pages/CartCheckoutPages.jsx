import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Trash2, ShoppingCart, ArrowRight, Truck, ShieldCheck } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { orderAPI } from '../services/api';
import { Spinner, Price } from '../components/common/UI';
import toast from 'react-hot-toast';

export function CartPage() {
  const cart = useCart();
  const { cart: cartData, loading, removeItem, updateItem } = cart || {};
  const items = cartData?.items || [];
  const total = cartData?.grandTotal || 0;

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><Spinner size={32} /></div>;

  return (
    <div className="container page-content">
      <h1 style={{ marginBottom: 28 }}>Shopping Cart</h1>

      {items.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '64px 0' }}>
          <ShoppingCart size={64} style={{ margin: '0 auto 20px', opacity: 0.15 }} />
          <h2 style={{ marginBottom: 8 }}>Your cart is empty</h2>
          <p className="text-muted" style={{ marginBottom: 24 }}>Add some products to get started.</p>
          <Link to="/shop"><button className="btn btn-primary btn-lg">Browse products</button></Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 28, alignItems: 'start' }}>
          {/* Items */}
          <div className="card">
            <div style={{ padding: '0 0' }}>
              {items.map((item, idx) => (
                <div key={item.cartItemId} style={{
                  display: 'flex', alignItems: 'center', gap: 16, padding: '16px 20px',
                  borderBottom: idx < items.length - 1 ? '1px solid var(--border)' : 'none',
                }}>
                  <div style={{
                    width: 72, height: 72, borderRadius: 'var(--radius-md)',
                    background: 'var(--surface-3)', flexShrink: 0,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    overflow: 'hidden',
                  }}>
                    {console.log("item", item)}
                    <span style={{ fontSize: 24, opacity: 0.3 }}>📦</span>
                  </div>
                  <div style={{ flex: 1 }}>
                    <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 15 }}>{item.productName}</p>
                    <p className="text-muted text-small">${Number(item.unitPrice).toFixed(2)} each</p>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ display: 'flex', alignItems: 'center', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-md)', overflow: 'hidden' }}>
                      <button className="btn btn-ghost" style={{ padding: '6px 12px', borderRadius: 0 }}
                        onClick={() => updateItem(item.cartItemId, item.quantity - 1)} disabled={item.quantity <= 1}>−</button>
                      <span style={{ padding: '6px 12px', fontWeight: 700, minWidth: 32, textAlign: 'center', fontSize: 14 }}>{item.quantity}</span>
                      <button className="btn btn-ghost" style={{ padding: '6px 12px', borderRadius: 0 }}
                        onClick={() => updateItem(item.cartItemId, item.quantity + 1)}>+</button>
                    </div>
                    <Price amount={item.lineTotal} style={{ fontSize: 16 }} />
                    <button className="btn btn-ghost" style={{ padding: 8, color: 'var(--red)' }}
                      onClick={() => {
                        console.log("clicked", item.cartItemId);
                        removeItem(item.cartItemId)
                        
                      }}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Summary */}
          <div className="card card-body" style={{ position: 'sticky', top: 88 }}>
            <h3 style={{ marginBottom: 20 }}>Order Summary</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 20 }}>
              {items.map(item => (
                <div key={item.cartItemId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
                  <span className="text-muted">{item.productName} ×{item.quantity}</span>
                  <span>${Number(item.lineTotal).toFixed(2)}</span>
                </div>
              ))}
            </div>
            <div className="divider" />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>Total</span>
              <Price amount={total} style={{ fontSize: 20 }} />
            </div>
            <div style={{ background: 'var(--green-bg)', borderRadius: 'var(--radius-md)', padding: '10px 14px', marginBottom: 16, fontSize: 13, color: 'var(--green)', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Truck size={15} /> Cash on Delivery — pay when you receive
            </div>
            <Link to="/checkout">
              <button className="btn btn-accent btn-full btn-lg">
                Checkout <ArrowRight size={16} />
              </button>
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── Checkout Page ─────────────────────────────────────────── */
export function CheckoutPage() {
  const { cart: cartData, fetchCart } = useCart() || {};
  const navigate = useNavigate();
  const items = cartData?.items || [];
  const total = cartData?.grandTotal || 0;

  const [form, setForm] = useState({
    shippingAddress: '',
    shippingCity: '',
    shippingPostalCode: '',
    contactPhone: '',
    deliveryNotes: '',
    paymentMethod: 'CASH_ON_DELIVERY',
  });
  const [loading, setLoading] = useState(false);

  const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (items.length === 0) { toast.error('Your cart is empty'); return; }
    try {
      setLoading(true);
      const res = await orderAPI.place(form);
      await fetchCart?.();
      toast.success('Order placed successfully!');
      navigate(`/orders/${res.data.data.orderId}`);
    } catch (err) {
      toast.error(err.userMessage || 'Failed to place order');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container page-content">
      <h1 style={{ marginBottom: 8 }}>Checkout</h1>
      <p className="text-muted" style={{ marginBottom: 32 }}>Review your details before placing the order</p>

      <form onSubmit={handleSubmit}>
        <div className="checkout-grid">
          {/* Left: shipping + payment */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            {/* Shipping */}
            <div className="card card-body">
              <h3 style={{ marginBottom: 20, display: 'flex', alignItems: 'center', gap: 10 }}>
                <Truck size={20} /> Delivery Details
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                <div className="form-group" style={{ gridColumn: '1/-1', marginBottom: 0 }}>
                  <label className="form-label">Street address *</label>
                  <input className="form-input" placeholder="House no, Street, Area" value={form.shippingAddress} onChange={set('shippingAddress')} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">City *</label>
                  <input className="form-input" placeholder="Islamabad" value={form.shippingCity} onChange={set('shippingCity')} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label">Postal code</label>
                  <input className="form-input" placeholder="44000" value={form.shippingPostalCode} onChange={set('shippingPostalCode')} />
                </div>
                <div className="form-group" style={{ gridColumn: '1/-1', marginBottom: 0 }}>
                  <label className="form-label">Contact phone *</label>
                  <input className="form-input" placeholder="0300-1234567" value={form.contactPhone} onChange={set('contactPhone')} required />
                  <span className="form-hint">The delivery rider will call this number</span>
                </div>
                <div className="form-group" style={{ gridColumn: '1/-1', marginBottom: 0 }}>
                  <label className="form-label">Delivery notes</label>
                  <input className="form-input" placeholder="e.g. Ring bell twice, leave at gate…" value={form.deliveryNotes} onChange={set('deliveryNotes')} />
                </div>
              </div>
            </div>

            {/* Payment */}
            <div className="card card-body">
              <h3 style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 10 }}>
                <ShieldCheck size={20} /> Payment Method
              </h3>
              <label style={{
                display: 'flex', alignItems: 'flex-start', gap: 14, padding: '16px 20px',
                border: '2px solid var(--ink)', borderRadius: 'var(--radius-md)',
                background: 'var(--surface-2)', cursor: 'pointer',
              }}>
                <input type="radio" name="payment" value="CASH_ON_DELIVERY" checked readOnly style={{ marginTop: 2 }} />
                <div>
                  <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 15 }}>💵 Cash on Delivery</p>
                  <p style={{ fontSize: 13, color: 'var(--ink-muted)', marginTop: 2 }}>Pay in cash when your order arrives. No online payment required.</p>
                </div>
              </label>
              <p style={{ fontSize: 12, color: 'var(--ink-light)', marginTop: 10 }}>More payment options coming soon.</p>
            </div>
          </div>

          {/* Right: order summary */}
          <div>
            <div className="card card-body" style={{ position: 'sticky', top: 88 }}>
              <h3 style={{ marginBottom: 20 }}>Your Order</h3>
              {items.map(item => (
                <div key={item.cartItemId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, marginBottom: 10 }}>
                  <span>{item.productName} <span className="text-muted">×{item.quantity}</span></span>
                  <span style={{ fontWeight: 600 }}>${Number(item.lineTotal).toFixed(2)}</span>
                </div>
              ))}
              <div className="divider" />
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
                <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>Total</span>
                <Price amount={total} style={{ fontSize: 20 }} />
              </div>
              <button type="submit" className="btn btn-accent btn-full btn-lg" disabled={loading}>
                {loading ? <Spinner white size={18} /> : '📦 Place Order'}
              </button>
              <p style={{ fontSize: 12, color: 'var(--ink-muted)', textAlign: 'center', marginTop: 12 }}>
                By placing the order, you agree to pay cash on delivery.
              </p>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
