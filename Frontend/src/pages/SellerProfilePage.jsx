import React, { useState, useEffect } from 'react';
import { Store, User, Save } from 'lucide-react';
import { sellerAPI, authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Spinner, InfoBar } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function SellerProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving]   = useState(false);
  const [tab, setTab]         = useState('store');

  const [storeForm, setStoreForm]   = useState({ storeName: '', description: '' });
  const [accountForm, setAccountForm] = useState({ name: '', newPassword: '' });

  useEffect(() => {
    sellerAPI.getMe()
      .then(r => {
        setProfile(r.data.data);
        setStoreForm({ storeName: r.data.data.storeName, description: r.data.data.description || '' });
      })
      .catch(() => {
        // No profile yet — allow creation
        if (user) setStoreForm(f => ({ ...f }));
      })
      .finally(() => setLoading(false));
    if (user) setAccountForm({ name: user.name, newPassword: '' });
  }, [user]);

  const saveStore = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      if (profile) await sellerAPI.update(storeForm);
      else         await sellerAPI.create(storeForm);
      toast.success('Store profile saved');
      const res = await sellerAPI.getMe();
      setProfile(res.data.data);
    } catch (err) { toast.error(err.userMessage || 'Failed'); }
    finally { setSaving(false); }
  };

  const saveAccount = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      await authAPI.updateProfile(accountForm);
      toast.success('Account updated');
    } catch (err) { toast.error(err.userMessage || 'Failed'); }
    finally { setSaving(false); }
  };

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}><Spinner size={32} /></div>;

  return (
    <div className="container page-content" style={{ maxWidth: 640 }}>
      <h1 style={{ marginBottom: 8 }}>Profile</h1>
      <p className="text-muted" style={{ marginBottom: 28 }}>Manage your store and account settings</p>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 24, background: 'var(--surface-3)', padding: 4, borderRadius: 'var(--radius-md)', width: 'fit-content' }}>
        {[{ id: 'store', label: 'Store', icon: Store }, { id: 'account', label: 'Account', icon: User }].map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            padding: '8px 18px', borderRadius: 'var(--radius-md)', border: 'none',
            background: tab === t.id ? 'var(--surface)' : 'transparent',
            color: tab === t.id ? 'var(--ink)' : 'var(--ink-muted)',
            fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 14,
            cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6,
            boxShadow: tab === t.id ? 'var(--shadow-sm)' : 'none',
            transition: 'all var(--transition)',
          }}>
            <t.icon size={15} /> {t.label}
          </button>
        ))}
      </div>

      {tab === 'store' && (
        <div className="card card-body">
          {!profile && (
            <InfoBar type="info" style={{ marginBottom: 16 }}>
              You haven't set up your store profile yet. Fill in the details below to start selling.
            </InfoBar>
          )}
          <form onSubmit={saveStore}>
            <div className="form-group">
              <label className="form-label">Store name *</label>
              <input className="form-input" value={storeForm.storeName}
                onChange={e => setStoreForm(f => ({ ...f, storeName: e.target.value }))} required placeholder="e.g. Ahmed's Electronics" />
            </div>
            <div className="form-group">
              <label className="form-label">Store description</label>
              <textarea className="form-input" value={storeForm.description}
                onChange={e => setStoreForm(f => ({ ...f, description: e.target.value }))}
                placeholder="Tell customers what you sell…" />
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? <Spinner white size={16} /> : <Save size={15} />}
              {profile ? 'Save changes' : 'Create store profile'}
            </button>
          </form>
        </div>
      )}

      {tab === 'account' && (
        <div className="card card-body">
          <form onSubmit={saveAccount}>
            <div className="form-group">
              <label className="form-label">Full name *</label>
              <input className="form-input" value={accountForm.name}
                onChange={e => setAccountForm(f => ({ ...f, name: e.target.value }))} required />
            </div>
            <div className="form-group">
              <label className="form-label">Email address</label>
              <input className="form-input" value={user?.email} disabled style={{ opacity: 0.6, cursor: 'not-allowed' }} />
              <span className="form-hint">Email cannot be changed</span>
            </div>
            <div className="form-group">
              <label className="form-label">New password</label>
              <input type="password" className="form-input" value={accountForm.newPassword}
                onChange={e => setAccountForm(f => ({ ...f, newPassword: e.target.value }))}
                placeholder="Leave blank to keep current password" />
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? <Spinner white size={16} /> : <Save size={15} />} Save account
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
