import React, { useState, useEffect, useCallback } from 'react';
import { Plus, Edit2, Trash2, Upload, Package, Image } from 'lucide-react';
import { productAPI, categoryAPI } from '../services/api';
import { Spinner, Modal, ConfirmModal, EmptyState, StatusBadge } from '../components/common/UI';
import toast from 'react-hot-toast';

export default function SellerProductsPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [page, setPage]         = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const [showForm, setShowForm]   = useState(false);
  const [editing, setEditing]     = useState(null);
  const [deleting, setDeleting]   = useState(null);
  const [showImgModal, setShowImgModal] = useState(null);
  const [imgFile, setImgFile]     = useState(null);
  const [uploadingImg, setUploadingImg] = useState(false);
  const [saving, setSaving]       = useState(false);

  const [form, setForm] = useState({ name: '', description: '', price: '', stock: '', categoryId: '' });
  const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await productAPI.sellerProducts(page, 20);
      setProducts(res.data.data?.content || []);
      setTotalPages(res.data.data?.totalPages || 1);
    } catch { setProducts([]); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { categoryAPI.list().then(r => setCategories(r.data.data || [])).catch(() => {}); }, []);

  const openNew = () => { setEditing(null); setForm({ name: '', description: '', price: '', stock: '', categoryId: '' }); setShowForm(true); };
  const openEdit = (p) => { setEditing(p); setForm({ name: p.name, description: p.description || '', price: p.price, stock: p.stock, categoryId: p.categoryId || '' }); setShowForm(true); };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      const payload = { ...form, price: Number(form.price), stock: Number(form.stock), categoryId: form.categoryId || null };
      if (editing) await productAPI.update(editing.id, payload);
      else         await productAPI.create(payload);
      toast.success(editing ? 'Product updated' : 'Product created');
      setShowForm(false);
      load();
    } catch (err) { toast.error(err.userMessage || 'Failed'); }
    finally { setSaving(false); }
  };

  const handleDelete = async () => {
    try {
      setSaving(true);
      await productAPI.delete(deleting.id);
      toast.success('Product deleted');
      setDeleting(null);
      load();
    } catch (err) { toast.error(err.userMessage || 'Failed'); }
    finally { setSaving(false); }
  };

  const handleImgUpload = async () => {
    if (!imgFile) return;
    try {
      setUploadingImg(true);
      await productAPI.uploadImage(showImgModal.id, imgFile);
      toast.success('Image uploaded');
      setShowImgModal(null);
      setImgFile(null);
      load();
    } catch (err) { toast.error(err.userMessage || 'Upload failed'); }
    finally { setUploadingImg(false); }
  };

  return (
    <div className="container page-content">
      <div className="flex-between" style={{ marginBottom: 28 }}>
        <div>
          <h1 style={{ marginBottom: 4 }}>My Products</h1>
          <p className="text-muted">Manage your product catalogue</p>
        </div>
        <button className="btn btn-primary" onClick={openNew}>
          <Plus size={16} /> New Product
        </button>
      </div>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 64 }}><Spinner size={32} /></div>
      ) : products.length === 0 ? (
        <div className="card card-body">
          <EmptyState icon={Package} title="No products yet" description="Start by adding your first product."
            action={<button className="btn btn-primary" onClick={openNew}><Plus size={16} /> Add product</button>} />
        </div>
      ) : (
        <div className="card">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Images</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {products.map(p => (
                  <tr key={p.id}>
                    <td>
                      <div>
                        <p style={{ fontWeight: 600, fontSize: 14 }}>{p.name}</p>
                        <p style={{ fontSize: 12, color: 'var(--ink-muted)', marginTop: 2 }}>{p.description?.substring(0, 50)}{p.description?.length > 50 ? '…' : ''}</p>
                      </div>
                    </td>
                    <td><span className="badge badge-default">{p.categoryName || '—'}</span></td>
                    <td style={{ fontFamily: 'var(--font-display)', fontWeight: 700 }}>${Number(p.price).toFixed(2)}</td>
                    <td>
                      <span className={p.stock === 0 ? 'badge badge-cancelled' : p.stock < 5 ? 'badge badge-pending' : 'badge badge-delivered'}>
                        {p.stock} units
                      </span>
                    </td>
                    <td><span style={{ fontSize: 13, color: 'var(--ink-muted)' }}>{p.imageUrls?.length || 0} photo{p.imageUrls?.length !== 1 ? 's' : ''}</span></td>
                    <td>
                      <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                        <button className="btn btn-ghost btn-sm" title="Upload image" onClick={() => setShowImgModal(p)}><Image size={15} /></button>
                        <button className="btn btn-ghost btn-sm" onClick={() => openEdit(p)}><Edit2 size={15} /></button>
                        <button className="btn btn-ghost btn-sm" style={{ color: 'var(--red)' }} onClick={() => setDeleting(p)}><Trash2 size={15} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Create / Edit Modal */}
      <Modal open={showForm} onClose={() => setShowForm(false)} title={editing ? 'Edit Product' : 'New Product'}
        footer={<>
          <button className="btn btn-outline" onClick={() => setShowForm(false)}>Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? <Spinner white size={16} /> : null} {editing ? 'Save changes' : 'Create product'}
          </button>
        </>}>
        <form onSubmit={handleSave}>
          <div className="form-group">
            <label className="form-label">Product name *</label>
            <input className="form-input" value={form.name} onChange={set('name')} required placeholder="e.g. Nike Air Max 2024" />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="form-input" value={form.description} onChange={set('description')} placeholder="Describe your product…" />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div className="form-group">
              <label className="form-label">Price ($) *</label>
              <input type="number" step="0.01" min="0.01" className="form-input" value={form.price} onChange={set('price')} required placeholder="29.99" />
            </div>
            <div className="form-group">
              <label className="form-label">Stock quantity *</label>
              <input type="number" min="0" className="form-input" value={form.stock} onChange={set('stock')} required placeholder="50" />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Category</label>
            <select className="form-input" value={form.categoryId} onChange={set('categoryId')}>
              <option value="">No category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
        </form>
      </Modal>

      {/* Image Upload Modal */}
      <Modal open={!!showImgModal} onClose={() => { setShowImgModal(null); setImgFile(null); }}
        title={`Upload Image — ${showImgModal?.name}`}
        footer={<>
          <button className="btn btn-outline" onClick={() => { setShowImgModal(null); setImgFile(null); }}>Cancel</button>
          <button className="btn btn-primary" onClick={handleImgUpload} disabled={!imgFile || uploadingImg}>
            {uploadingImg ? <Spinner white size={16} /> : <Upload size={15} />} Upload
          </button>
        </>}>
        <div style={{ border: '2px dashed var(--border)', borderRadius: 'var(--radius-lg)', padding: 32, textAlign: 'center' }}>
          <Upload size={32} style={{ margin: '0 auto 12px', opacity: 0.3 }} />
          <p className="text-muted" style={{ marginBottom: 16 }}>Select a JPEG or PNG image</p>
          <input type="file" accept="image/*" onChange={e => setImgFile(e.target.files[0])} style={{ display: 'block', margin: '0 auto', fontSize: 14 }} />
          {imgFile && <p style={{ marginTop: 12, fontSize: 13, color: 'var(--green)' }}>✓ {imgFile.name}</p>}
        </div>
      </Modal>

      <ConfirmModal open={!!deleting} onClose={() => setDeleting(null)} onConfirm={handleDelete} loading={saving}
        danger title="Delete Product" message={`Are you sure you want to delete "${deleting?.name}"? This cannot be undone.`} />
    </div>
  );
}
