import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Spinner } from '../components/common/UI';
import toast from 'react-hot-toast';

function AuthLayout({ title, subtitle, children }) {
  return (
    <div style={{
      minHeight: '100vh',
      background: 'var(--surface-2)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: 24,
    }}>
      <div style={{ width: '100%', maxWidth: 440 }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Link to="/" style={{
            fontFamily: 'var(--font-display)',
            fontWeight: 800,
            fontSize: 26,
            color: 'var(--ink)',
            letterSpacing: '-0.03em',
          }}>BAZAAR<span style={{ color: 'var(--accent)' }}>.</span></Link>
          <h1 style={{ color: 'var(--accent)', fontSize: 24, marginTop: 20, marginBottom: 6 }}>{title}</h1>
          <p className="text-muted text-small">{subtitle}</p>
        </div>
        <div className="card">
          <div className="card-body">{children}</div>
        </div>
      </div>
    </div>
  );
}

export function LoginPage() {
  const { login, isSeller } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const user = await login(form.email, form.password);
      toast.success(`Welcome back, ${user.name.split(' ')[0]}!`);
      if (user.role === 'SELLER') navigate('/seller/products');
      else navigate('/shop');
    } catch (err) {
      toast.error(err.userMessage || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to your account to continue">
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Email address</label>
          <input
            type="email" className="form-input" placeholder="you@example.com"
            value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
            required autoFocus
          />
        </div>
        <div className="form-group">
          <label className="form-label">Password</label>
          <input
            type="password" className="form-input" placeholder="••••••••"
            value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary btn-full" style={{ marginTop: 8 }} disabled={loading}>
          {loading ? <><Spinner white size={16} /> Signing in…</> : 'Sign in'}
        </button>
      </form>
      <div className="divider" />
      <p style={{ textAlign: 'center', fontSize: 14, color: 'var(--ink-muted)' }}>
        Don't have an account?{' '}
        <Link to="/register" style={{ color: 'var(--accent)', fontWeight: 600 }}>Create one</Link>
      </p>
    </AuthLayout>
  );
}

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'CUSTOMER' });
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setForm(f => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password.length < 6) { toast.error('Password must be at least 6 characters'); return; }
    try {
      setLoading(true);
      const user = await register(form);
      toast.success(`Account created! Welcome, ${user.name.split(' ')[0]}!`);
      if (user.role === 'SELLER') navigate('/seller/products');
      else navigate('/shop');
    } catch (err) {
      toast.error(err.userMessage || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Create account" subtitle="Join thousands of buyers and sellers">
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Full name</label>
          <input type="text" className="form-input" placeholder="Your name" value={form.name} onChange={set('name')} required />
        </div>
        <div className="form-group">
          <label className="form-label">Email address</label>
          <input type="email" className="form-input" placeholder="you@example.com" value={form.email} onChange={set('email')} required />
        </div>
        <div className="form-group">
          <label className="form-label">Password</label>
          <input type="password" className="form-input" placeholder="Min. 6 characters" value={form.password} onChange={set('password')} required />
        </div>
        <div className="form-group">
          <label className="form-label">I want to</label>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginTop: 4 }}>
            {[
              { val: 'CUSTOMER', label: '🛒 Shop', desc: 'Buy products' },
              { val: 'SELLER',   label: '🏪 Sell', desc: 'List products' },
            ].map(opt => (
              <label key={opt.val} style={{
                border: `2px solid ${form.role === opt.val ? 'var(--ink)' : 'var(--border)'}`,
                borderRadius: 'var(--radius-md)',
                padding: '12px 14px',
                cursor: 'pointer',
                transition: 'all var(--transition)',
                background: form.role === opt.val ? 'var(--surface-3)' : 'var(--surface)',
              }}>
                <input type="radio" name="role" value={opt.val} checked={form.role === opt.val} onChange={set('role')} style={{ display: 'none' }} />
                <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 14 }}>{opt.label}</div>
                <div style={{ fontSize: 12, color: 'var(--ink-muted)', marginTop: 2 }}>{opt.desc}</div>
              </label>
            ))}
          </div>
        </div>
        <button type="submit" className="btn btn-primary btn-full" style={{ marginTop: 8 }} disabled={loading}>
          {loading ? <><Spinner white size={16} /> Creating account…</> : 'Create account'}
        </button>
      </form>
      <div className="divider" />
      <p style={{ textAlign: 'center', fontSize: 14, color: 'var(--ink-muted)' }}>
        Already have an account?{' '}
        <Link to="/login" style={{ color: 'var(--accent)', fontWeight: 600 }}>Sign in</Link>
      </p>
    </AuthLayout>
  );
}
