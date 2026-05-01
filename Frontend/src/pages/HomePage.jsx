import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, ShieldCheck, Truck, RefreshCw, Headphones } from 'lucide-react';
import { productAPI, categoryAPI } from '../services/api';
import ProductCard from '../components/common/ProductCard';
import { Spinner } from '../components/common/UI';
import { useAuth } from '../context/AuthContext';

export default function HomePage() {
  const { isLoggedIn } = useAuth();
  const [featured, setFeatured]     = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading]       = useState(true);

  useEffect(() => {
    Promise.all([
      productAPI.list(0, 8, 'name'),
      categoryAPI.list(),
    ]).then(([prod, cat]) => {
      setFeatured(prod.data.data?.content || []);
      setCategories(cat.data.data?.slice(0, 6) || []);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const perks = [
    { icon: Truck,        title: 'Fast Delivery',        desc: 'Get your orders delivered to your doorstep' },
    { icon: ShieldCheck,  title: 'Secure Payments',      desc: 'Cash on Delivery — pay when you receive' },
    { icon: RefreshCw,    title: 'Easy Returns',         desc: 'Hassle-free returns within 7 days' },
    { icon: Headphones,   title: 'AI Shopping Help',     desc: 'Chat with our AI assistant anytime' },
  ];

  return (
    <div>
      {/* Hero */}
      <div style={{
        background: 'var(--accent)',
        color: '#fff',
        padding: '60px 30px 50px 50px',
        position: 'relative',
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        
       zIndex:2,
      }}>
        {/* Background texture */}
        <div style={{
          position: 'absolute', inset: 0, paddingLeft:'20px',
          backgroundImage: 'radial-gradient(circle at 80% 50%, rgba(245,235,224,0.7) 0%, rgba(245,235,224,0.3) 40%, transparent 70%)',
          pointerEvents: 'none',
        }} />
        <div className="container" style={{ position: 'relative' }}>
          <p style={{ fontSize: 12, fontWeight: 700, letterSpacing: '0.14em', textTransform: 'uppercase', color: 'var(--warm)', marginBottom: 16 }}>
            AI-Powered Ecommerce
          </p>
          <h1 style={{ fontSize: 'clamp(36px, 6vw, 64px)', fontWeight: 800, lineHeight: 1.1, marginBottom: 20, maxWidth: 600 }}>
            Shop smarter<br />with AI.
          </h1>
          <p style={{ fontSize: 18, color: 'rgba(255,255,255,0.65)', maxWidth: 460, lineHeight: 1.7, marginBottom: 36 }}>
            Discover thousands of products from verified sellers. Get personalised recommendations and shop with confidence.
          </p>
          <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
            <Link to="/shop">
              <button style={{
                padding: '14px 28px', background: 'var(--accent)', color: '#fff',
                border: 'none', borderRadius: 'var(--radius-md)',
                fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 15,
                cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8,
                transition: 'background var(--transition)',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--accent-2)'}
              onMouseLeave={e => e.currentTarget.style.background = 'var(--accent)'}>
                Browse Products <ArrowRight size={16} />
              </button>
            </Link>
            {!isLoggedIn && (
              <Link to="/register">
                  <button
                    style={{
                      padding: '14px 28px',
                      background: 'var(--surface)',              // warm white
                      color: 'var(--accent)',                   // hot pink text
                      border: '1.5px solid var(--border)',      // soft beige border
                      borderRadius: 'var(--radius-md)',
                      fontFamily: 'var(--font-display)',
                      fontWeight: 700,
                      fontSize: 15,
                      cursor: 'pointer',
                      transition: 'all var(--transition)',
                    }}
                    onMouseEnter={e => {
                      e.currentTarget.style.background = 'var(--accent)';
                      e.currentTarget.style.color = '#fff';
                      e.currentTarget.style.borderColor = 'var(--accent)';
                    }}
                    onMouseLeave={e => {
                      e.currentTarget.style.background = 'var(--surface)';
                      e.currentTarget.style.color = 'var(--accent)';
                      e.currentTarget.style.borderColor = 'var(--border)';
                    }}
                  >
                    Start selling →
                  </button>
                </Link>
            )}
          </div>
        </div>
        <div style={{
                    flex: 1,
                    display: 'flex',
                    justifyContent: 'flex-end',
                    alignItems: 'center',
                    height: '100%',
                    width: '100%',
                  }}>
                    <img
                      src="/public/homepagebg.png"
                      alt="Hero"
                       style={{
    position: 'absolute',
    right: '15px',
    bottom: '10px',
    height: '100%',              // 🔥 fills most of hero height
    maxHeight: '650px',
    width: 'auto',
    objectFit: 'contain',
    filter: 'brightness(1.2) contrast(1.05)',
    pointerEvents: 'none',
    
    zIndex: 1,
  }}
                    />
</div>
      </div>

      {/* Perks bar */}
      <div style={{ background: 'var(--surface)', borderBottom: '1px solid var(--border)' }}>
        <div className="container">
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 0 }}>
            {perks.map((p, i) => (
              <div key={i} style={{
                padding: '20px 24px',
                borderRight: i < 3 ? '1px solid var(--border)' : 'none',
                display: 'flex', alignItems: 'center', gap: 14,
              }}>
                <div style={{ width: 40, height: 40, borderRadius: 'var(--radius-md)', background: 'var(--surface-2)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <p.icon size={18} style={{ color: 'var(--accent)' }} />
                </div>
                <div>
                  <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 13, marginBottom: 2 }}>{p.title}</p>
                  <p style={{ fontSize: 12, color: 'var(--ink-muted)' }}>{p.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="container page-content">

        {/* Categories */}
        {categories.length > 0 && (
          <div style={{ marginBottom: 52 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
              <h2>Browse by category</h2>
            </div>
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
              {categories.map(c => (
                <Link key={c.id} to={`/shop?categoryId=${c.id}`}>
                  <div style={{
                    padding: '10px 20px',
                    background: 'var(--surface)',
                    border: '1.5px solid var(--border)',
                    borderRadius: 100,
                    fontFamily: 'var(--font-display)',
                    fontWeight: 600,
                    fontSize: 14,
                    cursor: 'pointer',
                    transition: 'all var(--transition)',
                    color: 'var(--ink)',
                  }}
                  onMouseEnter={e => { e.currentTarget.style.borderColor = 'var(--ink)'; e.currentTarget.style.background = 'var(--surface-3)'; }}
                  onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border)'; e.currentTarget.style.background = 'var(--surface)'; }}>
                    {c.name}
                  </div>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Featured products */}
        <div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
            <h2>Featured products</h2>
            <Link to="/shop">
              <button className="btn btn-ghost btn-sm">View all <ArrowRight size={14} /></button>
            </Link>
          </div>
          {loading ? (
            <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}><Spinner size={32} /></div>
          ) : (
            <div className="product-grid">
              {featured.map(p => <ProductCard key={p.id} product={p} />)}
            </div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div style={{ background: 'var(--accent)', color: 'rgba(255,255,255,0.5)', padding: '40px 0', marginTop: 32 }}>
        <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 16 }}>
          <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 18, color: '#fff' }}>
            BAZAAR<span style={{ color: 'var(--accent)' }}>.</span>
          </span>
          <p style={{ fontSize: 13 }}>AI-Powered Ecommerce · Built with Spring Boot & React</p>
        </div>
      </div>
    </div>
  );
}
