import React, { useState, useEffect, useCallback } from 'react';
import { ChevronRight, MapPin, Phone, FileText, CheckCircle, Truck, Package, XCircle } from 'lucide-react';
import { sellerOrderAPI } from '../services/api';
import { Spinner, Modal, StatusBadge, OrderTimeline, Pagination, InfoBar } from '../components/common/UI';
import toast from 'react-hot-toast';

/* ─── Helper: next allowed status ─────────────────────────── */
const NEXT = { PENDING: 'CONFIRMED', CONFIRMED: 'SHIPPED', SHIPPED: 'DELIVERED' };
const NEXT_LABEL = {
  PENDING:   { label: 'Confirm Order',    icon: CheckCircle, style: 'btn-primary' },
  CONFIRMED: { label: 'Mark as Shipped',  icon: Truck,       style: 'btn-accent' },
  SHIPPED:   { label: 'Mark Delivered',   icon: Package,     style: 'btn-accent' },
};
const NOTE_PLACEHOLDER = {
  PENDING:   'Optional note for the customer…',
  CONFIRMED: 'Optional note — e.g. expected dispatch date',
  SHIPPED:   'Tracking number / courier name (highly recommended)',
};

/* ─── Order row card ────────────────────────────────────────── */
function OrderRow({ order, onOpen }) {
  return (
    <div className="card" style={{ cursor: 'pointer', transition: 'box-shadow var(--transition)' }}
      onClick={() => onOpen(order)}
      onMouseEnter={e => e.currentTarget.style.boxShadow = 'var(--shadow-md)'}
      onMouseLeave={e => e.currentTarget.style.boxShadow = ''}>
      <div style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
        <div style={{ flex: 1, minWidth: 200 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
            <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>Order #{order.orderId}</span>
            <StatusBadge status={order.status} />
          </div>
          <p style={{ fontSize: 13, color: 'var(--ink-muted)' }}>
            <strong style={{ color: 'var(--ink)' }}>{order.customerName || 'Customer'}</strong>
            {' · '}{new Date(order.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
            {' · '}{order.items?.length} item{order.items?.length !== 1 ? 's' : ''}
          </p>
          <p style={{ fontSize: 12, color: 'var(--ink-light)', marginTop: 2, display: 'flex', alignItems: 'center', gap: 4 }}>
            <MapPin size={11} /> {order.shippingCity}
            {' · '}
            <Phone size={11} /> {order.contactPhone}
          </p>
        </div>
        <div style={{ textAlign: 'right', display: 'flex', alignItems: 'center', gap: 16 }}>
          <div>
            <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 18 }}>
              ${Number(order.totalPrice).toFixed(2)}
            </p>
            <p style={{ fontSize: 12, color: 'var(--ink-muted)' }}>COD</p>
          </div>
          <ChevronRight size={18} style={{ color: 'var(--ink-light)' }} />
        </div>
      </div>
    </div>
  );
}

/* ─── Order detail modal ────────────────────────────────────── */
function OrderDetailModal({ order, open, onClose, onRefresh }) {
  const [statusNote, setStatusNote] = useState('');
  const [advancing, setAdvancing]   = useState(false);
  const [showCancel, setShowCancel] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling]     = useState(false);

  if (!order) return null;

  const nextStatus = NEXT[order.status];
  const nextMeta   = NEXT_LABEL[order.status];
  const NextIcon   = nextMeta?.icon;

  const advance = async () => {
    if (!nextStatus) return;
    try {
      setAdvancing(true);
      await sellerOrderAPI.updateStatus(order.orderId, {
        status: nextStatus,
        statusNote: statusNote.trim() || undefined,
      });
      toast.success(`Order marked as ${nextStatus.toLowerCase()}`);
      setStatusNote('');
      onClose();
      onRefresh();
    } catch (err) {
      toast.error(err.userMessage || 'Failed to update status');
    } finally {
      setAdvancing(false);
    }
  };

  const cancelOrder = async () => {
    if (cancelReason.trim().length < 10) {
      toast.error('Please provide a reason (min. 10 characters)');
      return;
    }
    try {
      setCancelling(true);
      await sellerOrderAPI.cancel(order.orderId, { reason: cancelReason.trim() });
      toast.success('Order cancelled');
      setCancelReason('');
      setShowCancel(false);
      onClose();
      onRefresh();
    } catch (err) {
      toast.error(err.userMessage || 'Failed to cancel');
    } finally {
      setCancelling(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title={`Order #${order.orderId}`} wide
      footer={
        order.status !== 'CANCELLED' && order.status !== 'DELIVERED' ? (
          <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
            <button className="btn btn-danger" onClick={() => setShowCancel(s => !s)}>
              <XCircle size={15} /> Cancel Order
            </button>
            {nextMeta && (
              <button className={`btn ${nextMeta.style}`} onClick={advance} disabled={advancing}>
                {advancing ? <Spinner white size={16} /> : <NextIcon size={15} />}
                {nextMeta.label}
              </button>
            )}
          </div>
        ) : null
      }>

      {/* Status */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 14 }}>
          <StatusBadge status={order.status} />
          <span style={{ fontSize: 13, color: 'var(--ink-muted)' }}>
            Last updated: {order.updatedAt ? new Date(order.updatedAt).toLocaleString() : '—'}
          </span>
        </div>
        {order.status !== 'CANCELLED' && <OrderTimeline status={order.status} />}
        {order.status === 'CANCELLED' && (
          <div className="notif-bar error" style={{ marginTop: 12 }}>
            <div>
              <strong>Cancelled by {order.cancelledBy?.toLowerCase()}</strong>
              {order.cancellationReason && <p style={{ marginTop: 4 }}>{order.cancellationReason}</p>}
            </div>
          </div>
        )}
        {order.statusNote && (
          <div style={{ marginTop: 12, padding: '10px 14px', background: 'var(--blue-bg)', borderRadius: 'var(--radius-md)', fontSize: 13, color: 'var(--blue)' }}>
            <strong>Previous note:</strong> {order.statusNote}
          </div>
        )}
      </div>

      {/* Status note input */}
      {nextStatus && order.status !== 'CANCELLED' && !showCancel && (
        <div style={{ marginBottom: 20 }}>
          <label className="form-label" style={{ marginBottom: 6, display: 'block' }}>
            Note for customer when marking as {nextStatus.toLowerCase()} (optional)
          </label>
          <input
            className="form-input"
            placeholder={NOTE_PLACEHOLDER[order.status]}
            value={statusNote}
            onChange={e => setStatusNote(e.target.value)}
          />
          {order.status === 'CONFIRMED' && (
            <span className="form-hint">Tracking number will be shown to the customer in their order detail.</span>
          )}
        </div>
      )}

      {/* Cancel reason box */}
      {showCancel && (
        <div style={{ marginBottom: 20 }}>
          <div className="notif-bar warning" style={{ marginBottom: 12 }}>
            You are about to cancel this order. Stock will be automatically restored.
          </div>
          <div className="form-group">
            <label className="form-label">Cancellation reason * <span style={{ color: 'var(--red)' }}>(required, min. 10 chars)</span></label>
            <textarea
              className="form-input"
              placeholder="e.g. Item is out of stock, supplier delay, unable to deliver to this area…"
              value={cancelReason}
              onChange={e => setCancelReason(e.target.value)}
              style={{ minHeight: 80 }}
            />
          </div>
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn btn-outline btn-sm" onClick={() => { setShowCancel(false); setCancelReason(''); }}>Back</button>
            <button className="btn btn-danger" onClick={cancelOrder} disabled={cancelling || cancelReason.trim().length < 10}>
              {cancelling ? <Spinner size={15} /> : <XCircle size={15} />}
              {cancelling ? 'Cancelling…' : 'Confirm Cancellation'}
            </button>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 20 }}>
        {/* Customer info */}
        <div style={{ background: 'var(--surface-2)', borderRadius: 'var(--radius-md)', padding: 16 }}>
          <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 13, marginBottom: 10, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--ink-muted)' }}>Customer</p>
          <p style={{ fontWeight: 600, fontSize: 14 }}>{order.customerName}</p>
          <p style={{ fontSize: 13, color: 'var(--ink-muted)' }}>{order.customerEmail}</p>
        </div>

        {/* Delivery info */}
        <div style={{ background: 'var(--surface-2)', borderRadius: 'var(--radius-md)', padding: 16 }}>
          <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 13, marginBottom: 10, textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--ink-muted)' }}>
            <MapPin size={12} style={{ display: 'inline' }} /> Delivery
          </p>
          <p style={{ fontSize: 14, lineHeight: 1.6 }}>{order.shippingAddress}</p>
          <p style={{ fontSize: 14 }}>{order.shippingCity}{order.shippingPostalCode && `, ${order.shippingPostalCode}`}</p>
          <p style={{ fontSize: 13, color: 'var(--ink-muted)', marginTop: 6, display: 'flex', alignItems: 'center', gap: 5 }}>
            <Phone size={12} /> {order.contactPhone}
          </p>
          {order.deliveryNotes && (
            <p style={{ fontSize: 12, color: 'var(--ink-muted)', marginTop: 6, fontStyle: 'italic' }}>"{order.deliveryNotes}"</p>
          )}
        </div>
      </div>

      {/* Items */}
      <div style={{ border: '1px solid var(--border)', borderRadius: 'var(--radius-md)', overflow: 'hidden' }}>
        <div style={{ padding: '10px 16px', background: 'var(--surface-2)', fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 13, letterSpacing: '0.04em', color: 'var(--ink-muted)', textTransform: 'uppercase' }}>
          Items ({order.items?.length})
        </div>
        {order.items?.map((item, idx) => (
          <div key={item.orderItemId} style={{
            display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            padding: '12px 16px',
            borderTop: idx > 0 ? '1px solid var(--border)' : 'none',
            fontSize: 14,
          }}>
            <div>
              <span style={{ fontWeight: 600 }}>{item.productName}</span>
              <span style={{ color: 'var(--ink-muted)', marginLeft: 8 }}>×{item.quantity}</span>
            </div>
            <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>${Number(item.lineTotal).toFixed(2)}</span>
          </div>
        ))}
        <div style={{ padding: '12px 16px', borderTop: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', background: 'var(--surface-2)' }}>
          <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>Total</span>
          <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 18 }}>${Number(order.totalPrice).toFixed(2)}</span>
        </div>
      </div>

      <div style={{ marginTop: 14, padding: '10px 14px', background: 'var(--amber-bg)', borderRadius: 'var(--radius-md)', fontSize: 13, color: 'var(--amber)', display: 'flex', alignItems: 'center', gap: 8 }}>
        💵 Cash on Delivery — collect ${Number(order.totalPrice).toFixed(2)} on arrival
      </div>
    </Modal>
  );
}

/* ─── Main Page ─────────────────────────────────────────────── */
export default function SellerOrdersPage() {
  const [orders, setOrders]     = useState([]);
  const [loading, setLoading]   = useState(true);
  const [page, setPage]         = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [selected, setSelected] = useState(null);
  const [filter, setFilter]     = useState('ALL');

  const STATUS_FILTERS = ['ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await sellerOrderAPI.list(page);
      setOrders(res.data.data?.content || []);
      setTotalPages(res.data.data?.totalPages || 1);
    } catch { setOrders([]); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const openOrder = async (order) => {
    try {
      const res = await sellerOrderAPI.getById(order.orderId);
      setSelected(res.data.data);
    } catch { toast.error('Failed to load order'); }
  };

  const filtered = filter === 'ALL' ? orders : orders.filter(o => o.status === filter);

  // Status counts for tab badges
  const counts = orders.reduce((acc, o) => { acc[o.status] = (acc[o.status] || 0) + 1; return acc; }, {});

  return (
    <div className="container page-content">
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ marginBottom: 4 }}>Orders</h1>
        <p className="text-muted">Manage incoming orders and update delivery status</p>
      </div>

      {/* Filter tabs */}
      <div style={{ display: 'flex', gap: 6, marginBottom: 20, flexWrap: 'wrap' }}>
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            style={{
              padding: '6px 14px',
              borderRadius: 100,
              border: `1.5px solid ${filter === s ? 'var(--ink)' : 'var(--border)'}`,
              background: filter === s ? 'var(--ink)' : 'var(--surface)',
              color: filter === s ? '#fff' : 'var(--ink-muted)',
              fontSize: 13,
              fontWeight: 600,
              fontFamily: 'var(--font-display)',
              cursor: 'pointer',
              transition: 'all var(--transition)',
              display: 'flex', alignItems: 'center', gap: 6,
            }}
          >
            {s === 'ALL' ? 'All' : s.charAt(0) + s.slice(1).toLowerCase()}
            {s !== 'ALL' && counts[s] ? (
              <span style={{
                background: filter === s ? 'rgba(255,255,255,0.2)' : 'var(--surface-3)',
                color: filter === s ? '#fff' : 'var(--ink)',
                borderRadius: 100, padding: '1px 7px', fontSize: 11,
              }}>{counts[s]}</span>
            ) : null}
          </button>
        ))}
      </div>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}><Spinner size={32} /></div>
      ) : filtered.length === 0 ? (
        <div className="card card-body">
          <div className="empty-state">
            <Package size={48} strokeWidth={1.2} />
            <div>
              <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 16, color: 'var(--ink)', marginBottom: 4 }}>
                {filter === 'ALL' ? 'No orders yet' : `No ${filter.toLowerCase()} orders`}
              </p>
              <p>{filter === 'ALL' ? 'Orders from customers will appear here.' : 'Try a different filter.'}</p>
            </div>
          </div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {filtered.map(order => (
            <OrderRow key={order.orderId} order={order} onOpen={openOrder} />
          ))}
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </div>
      )}

      <OrderDetailModal
        order={selected}
        open={!!selected}
        onClose={() => setSelected(null)}
        onRefresh={load}
      />
    </div>
  );
}
