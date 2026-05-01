import React, { useState } from 'react';
import { User, Save, LogOut } from 'lucide-react';
import { authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Spinner } from '../components/common/UI';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: user?.name || '', newPassword: '' });
  const [saving, setSaving] = useState(false);

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      await authAPI.updateProfile(form);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.userMessage || 'Failed');
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => { logout(); navigate('/'); };

  return (
    <div className="container page-content" style={{ maxWidth: 560 }}>
      <h1 style={{color: 'var(--ink)', marginBottom: 28 }}>My Profile</h1>

      {/* Avatar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 20, marginBottom: 32 }}>
        <div style={{
          width: 72, height: 72, borderRadius: '50%',
          background: 'var(--accent)', color: 'var(--surface-2)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 28, fontFamily: 'var(--font-display)', fontWeight: 800,
        }}>
          {user?.name?.[0]?.toUpperCase()}
        </div>
        <div>
          <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 20 }}>{user?.name}</p>
          <p className="text-muted">{user?.email}</p>
          <span className="badge badge-accent" style={{ marginTop: 4 }}>{user?.role}</span>
        </div>
      </div>

      <div className="card card-body">
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label className="form-label">Full name *</label>
            <input className="form-input" value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
          </div>
          <div className="form-group">
            <label className="form-label">Email address</label>
            <input className="form-input" value={user?.email} disabled style={{ opacity: 0.6 }} />
            <span className="form-hint">Email cannot be changed</span>
          </div>
          <div className="form-group">
            <label className="form-label">New password</label>
            <input type="password" className="form-input" value={form.newPassword}
              onChange={e => setForm(f => ({ ...f, newPassword: e.target.value }))}
              placeholder="Leave blank to keep current password" />
          </div>
          <div style={{ display: 'flex', gap: 12 }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? <Spinner white size={16} /> : <Save size={15} />} Save changes
            </button>
            <button type="button" className="btn btn-outline" onClick={handleLogout}>
              <LogOut size={15} /> Logout
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
