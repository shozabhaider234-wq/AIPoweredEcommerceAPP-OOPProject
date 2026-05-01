import React, { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send, Sparkles } from 'lucide-react';
import { chatAPI } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { Spinner } from './UI';

export default function ChatWidget() {
  const { user, isLoggedIn } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'ai', text: "Hi! I'm your AI shopping assistant. Ask me about products, get recommendations, or check your order status." }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  if (!isLoggedIn) return null;

  const send = async () => {
    const msg = input.trim();
    if (!msg || loading) return;
    setInput('');
    setMessages(prev => [...prev, { role: 'user', text: msg }]);
    setLoading(true);
    try {
      const res = await chatAPI.send(user.id, msg);
      setMessages(prev => [...prev, { role: 'ai', text: res.data.data.reply }]);
    } catch {
      setMessages(prev => [...prev, { role: 'ai', text: 'Sorry, I had trouble responding. Please try again.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chat-widget">
      {open && (
        <div className="chat-panel">
          <div className="chat-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <Sparkles size={18} />
              <span className="chat-title">AI Assistant</span>
            </div>
            <button onClick={() => setOpen(false)} style={{ background: 'none', border: 'none', color: 'rgba(255,255,255,0.7)', cursor: 'pointer', display: 'flex' }}>
              <X size={18} />
            </button>
          </div>
          <div className="chat-messages">
            {messages.map((m, i) => (
              <div key={i} className={`chat-msg ${m.role}`} style={{ whiteSpace: 'pre-wrap' }}>
                {m.text}
              </div>
            ))}
            {loading && (
              <div className="chat-msg ai" style={{ display: 'flex', gap: 6, alignItems: 'center', color: 'var(--ink-muted)' }}>
                <Spinner size={14} />
                <span style={{ fontSize: 13 }}>Thinking…</span>
              </div>
            )}
            <div ref={endRef} />
          </div>
          <div className="chat-input-row">
            <input
              placeholder="Ask me anything…"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && send()}
              disabled={loading}
            />
            <button
              className="btn btn-primary btn-sm"
              style={{ padding: '7px 12px', borderRadius: '50%' }}
              onClick={send}
              disabled={!input.trim() || loading}
            >
              <Send size={15} />
            </button>
          </div>
        </div>
      )}
      <button className="chat-bubble-btn" onClick={() => setOpen(o => !o)} title="AI Assistant">
        {open ? <X size={22} /> : <MessageCircle size={22} />}
      </button>
    </div>
  );
}
