import React from 'react';
import { X, Star } from 'lucide-react';

/* ─── Spinner ──────────────────────────────────────────────── */
export function Spinner({ white = false, size = 20 }) {
  return (
    <span style={{
      display: 'inline-block',
      width: size, height: size,
      border: `2px solid ${white ? 'rgba(255,255,255,0.3)' : 'var(--border)'}`,
      borderTopColor: white ? '#fff' : 'var(--ink)',
      borderRadius: '50%',
      animation: 'spin 0.7s linear infinite',
    }} />
  );
}

/* ─── Modal ────────────────────────────────────────────────── */
export function Modal({ open, onClose, title, children, footer, wide }) {
  if (!open) return null;
  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose?.()}>
      <div className="modal" style={{ maxWidth: wide ? 680 : 520 }}>
        <div className="modal-header">
          <h3 className="modal-title">{title}</h3>
          {onClose && (
            <button onClick={onClose} className="btn btn-ghost" style={{ padding: 6 }}>
              <X size={18} />
            </button>
          )}
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}

/* ─── Empty State ──────────────────────────────────────────── */
export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="empty-state">
      {Icon && <Icon size={48} strokeWidth={1.2} />}
      <div>
        <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 16, color: 'var(--ink)', marginBottom: 4 }}>{title}</p>
        {description && <p>{description}</p>}
      </div>
      {action}
    </div>
  );
}

/* ─── Stars ────────────────────────────────────────────────── */
export function StarRating({ value = 0, max = 5, size = 16, onRate }) {
  const [hover, setHover] = React.useState(0);
  return (
    <div className="stars" style={{ cursor: onRate ? 'pointer' : 'default' }}>
      {Array.from({ length: max }, (_, i) => {
        const filled = (hover || value) > i;
        return (
          <Star
            key={i}
            size={size}
            fill={filled ? '#f59e0b' : 'none'}
            color={filled ? '#f59e0b' : '#d1d5db'}
            onMouseEnter={() => onRate && setHover(i + 1)}
            onMouseLeave={() => onRate && setHover(0)}
            onClick={() => onRate?.(i + 1)}
          />
        );
      })}
    </div>
  );
}

/* ─── Order Status Badge ───────────────────────────────────── */
export function StatusBadge({ status }) {
  const map = {
    PENDING:   'badge-pending',
    CONFIRMED: 'badge-confirmed',
    SHIPPED:   'badge-shipped',
    DELIVERED: 'badge-delivered',
    CANCELLED: 'badge-cancelled',
  };
  return <span className={`badge ${map[status] || 'badge-default'}`}>{status}</span>;
}

/* ─── Order Timeline ───────────────────────────────────────── */
export function OrderTimeline({ status }) {
  const steps = ['PENDING','CONFIRMED','SHIPPED','DELIVERED'];
  const idx = steps.indexOf(status);
  const isCancelled = status === 'CANCELLED';

  return (
    <div className="status-steps">
      {steps.map((s, i) => {
        let cls = '';
        if (isCancelled) cls = i === 0 ? 'cancelled' : '';
        else if (i < idx) cls = 'done';
        else if (i === idx) cls = 'active';
        return (
          <div key={s} className={`status-step ${cls}`}>
            <div className="status-dot">
              {cls === 'done' ? '✓' : cls === 'active' ? '●' : ''}
            </div>
            <span className="status-label">{s.charAt(0) + s.slice(1).toLowerCase()}</span>
          </div>
        );
      })}
    </div>
  );
}

/* ─── Pagination ───────────────────────────────────────────── */
export function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null;
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, marginTop: 32 }}>
      <button className="btn btn-outline btn-sm" disabled={page === 0} onClick={() => onChange(page - 1)}>← Prev</button>
      <span style={{ fontSize: 13, color: 'var(--ink-muted)' }}>Page {page + 1} of {totalPages}</span>
      <button className="btn btn-outline btn-sm" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>Next →</button>
    </div>
  );
}

/* ─── Price formatter ──────────────────────────────────────── */
export function Price({ amount, style }) {
  return (
    <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, ...style }}>
      ${Number(amount).toFixed(2)}
    </span>
  );
}

/* ─── Confirm dialog ───────────────────────────────────────── */
export function ConfirmModal({ open, onClose, onConfirm, title, message, danger, loading }) {
  return (
    <Modal open={open} onClose={onClose} title={title}
      footer={
        <>
          <button className="btn btn-outline" onClick={onClose}>Cancel</button>
          <button
            className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`}
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? <Spinner size={16} white={!danger} /> : 'Confirm'}
          </button>
        </>
      }
    >
      <p style={{ color: 'var(--ink-muted)' }}>{message}</p>
    </Modal>
  );
}

/* ─── Info bar ─────────────────────────────────────────────── */
export function InfoBar({ type = 'info', children }) {
  return <div className={`notif-bar ${type}`}>{children}</div>;
}
