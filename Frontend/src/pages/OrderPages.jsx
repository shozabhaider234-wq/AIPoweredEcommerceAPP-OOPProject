import React, { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Package, ChevronLeft, MapPin, Phone, Truck, CreditCard, MessageSquare } from 'lucide-react';
import { orderAPI } from '../services/api';
import { Spinner, StatusBadge, OrderTimeline, Pagination, ConfirmModal, InfoBar } from '../components/common/UI';
import toast from 'react-hot-toast';

export function OrdersPage() {
  const [orders, setOrders]     = useState([]);
  const [loading, setLoading]   = useState(true);
  const [page, setPage]         = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    setLoading(true);
    orderAPI.list(page)
      .then(r => {
        setOrders(r.data.data?.content || []);
        setTotalPages(r.data.data?.totalPages || 1);
      })
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div className="container page-content">
      <h1 style={{ marginBottom: 28 }}>My Orders</h1>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}><Spinner size={32} /></div>
      ) : orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '64px 0' }}>
          <Package size={64} style={{ margin: '0 auto 20px', opacity: 0.15 }} />
          <h2 style={{ marginBottom: 8 }}>No orders yet</h2>
          <p className="text-muted" style={{ marginBottom: 24 }}>Once you place an order, it will appear here.</p>
          <Link to="/shop"><button className="btn btn-primary">Start shopping</button></Link>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {orders.map(order => (
            <Link key={order.orderId} to={`/orders/${order.orderId}`}>
              <div className="card" style={{ transition: 'box-shadow var(--transition)' }}
                onMouseEnter={e => e.currentTarget.style.boxShadow = 'var(--shadow-md)'}
                onMouseLeave={e => e.currentTarget.style.boxShadow = ''}>
                <div style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 4 }}>
                      <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>Order #{order.orderId}</span>
                      <StatusBadge status={order.status} />
                    </div>
                    <p className="text-muted text-small">{new Date(order.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} · {order.items?.length} item{order.items?.length !== 1 ? 's' : ''}</p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 18 }}>${Number(order.totalPrice).toFixed(2)}</p>
                    <p className="text-muted text-small">{order.paymentMethod?.replace(/_/g, ' ')}</p>
                  </div>
                </div>
              </div>
            </Link>
          ))}
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      )}
    </div>
  );
}

/* ─── Order Detail Page ─────────────────────────────────────── */
export function OrderDetailPage() {
  const { id } = useParams();
  const [order, setOrder]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);
  const [showCancel, setShowCancel] = useState(false);

  const reload = () => orderAPI.getById(id).then(r => setOrder(r.data.data)).catch(() => {});

  useEffect(() => {
    orderAPI.getById(id)
      .then(r => setOrder(r.data.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [id]);

  const cancelOrder = async () => {
    try {
      setCancelling(true);
      await orderAPI.cancel(id);
      toast.success('Order cancelled');
      setShowCancel(false);
      reload();
    } catch (err) {
      toast.error(err.userMessage || 'Cannot cancel');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}><Spinner size={32} /></div>;
  if (!order) return <div className="container page-content"><InfoBar type="error">Order not found.</InfoBar></div>;

  return (
    <div className="container page-content">
      <Link to="/orders" style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 14, color: 'var(--ink-muted)', marginBottom: 24 }}>
        <ChevronLeft size={16} /> My orders
      </Link>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28, flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ marginBottom: 4 }}>Order #{order.orderId}</h1>
          <p className="text-muted text-small">Placed {new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <StatusBadge status={order.status} />
          {order.status === 'PENDING' && (
            <button className="btn btn-danger btn-sm" onClick={() => setShowCancel(true)}>Cancel Order</button>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 24, alignItems: 'start' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>

          {/* Status tracker */}
          <div className="card card-body">
            <h3 style={{ marginBottom: 20 }}>Order Status</h3>
            {order.status === 'CANCELLED' ? (
              <div>
                <div className="notif-bar error">
                  <div>
                    <strong>Order Cancelled</strong>
                    {order.cancelledBy && <span> by {order.cancelledBy.toLowerCase()}</span>}
                    {order.cancellationReason && <p style={{ marginTop: 4 }}>{order.cancellationReason}</p>}
                  </div>
                </div>
              </div>
            ) : (
              <OrderTimeline status={order.status} />
            )}
            {order.statusNote && (
              <div style={{ marginTop: 16, padding: '12px 14px', background: 'var(--blue-bg)', borderRadius: 'var(--radius-md)', fontSize: 14 }}>
                <strong style={{ color: 'var(--blue)' }}>Seller update:</strong>
                <p style={{ color: 'var(--ink-muted)', marginTop: 4 }}>{order.statusNote}</p>
              </div>
            )}
          </div>

          {/* Items */}
          <div className="card">
            <div className="card-header"><h3 className="card-title">Items Ordered</h3></div>
            {order.items?.map(item => (
              <div key={item.orderItemId} style={{ display: 'flex', justifyContent: 'space-between', padding: '14px 20px', borderBottom: '1px solid var(--border)', fontSize: 14 }}>
                <span style={{ fontWeight: 500 }}>{item.productName} <span className="text-muted">×{item.quantity}</span></span>
                <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>${Number(item.lineTotal).toFixed(2)}</span>
              </div>
            ))}
            <div style={{ padding: '14px 20px', display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>Total</span>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 18 }}>${Number(order.totalPrice).toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Sidebar: shipping + payment */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div className="card card-body">
            <h4 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
              <MapPin size={16} /> Delivery Address
            </h4>
            <p style={{ fontSize: 14, lineHeight: 1.7, color: 'var(--ink-muted)' }}>
              {order.shippingAddress}<br />
              {order.shippingCity}{order.shippingPostalCode && `, ${order.shippingPostalCode}`}
            </p>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10, fontSize: 14, color: 'var(--ink-muted)' }}>
              <Phone size={14} /> {order.contactPhone}
            </div>
            {order.deliveryNotes && (
              <div style={{ marginTop: 10, fontSize: 13, color: 'var(--ink-muted)', fontStyle: 'italic' }}>
                "{order.deliveryNotes}"
              </div>
            )}
          </div>

          <div className="card card-body">
            <h4 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: 14, display: 'flex', alignItems: 'center', gap: 8 }}>
              <CreditCard size={16} /> Payment
            </h4>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14 }}>
              <span className="text-muted">Method</span>
              <span style={{ fontWeight: 600 }}>💵 Cash on Delivery</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, marginTop: 8 }}>
              <span className="text-muted">Status</span>
              <span className={`badge ${order.paymentStatus === 'PAID' ? 'badge-delivered' : 'badge-pending'}`}>{order.paymentStatus}</span>
            </div>
          </div>
        </div>
      </div>

      <ConfirmModal
        open={showCancel}
        onClose={() => setShowCancel(false)}
        onConfirm={cancelOrder}
        loading={cancelling}
        danger
        title="Cancel Order"
        message="Are you sure you want to cancel this order? This action cannot be undone."
      />
    </div>
  );
}
