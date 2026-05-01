import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { cartAPI } from '../services/api';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isLoggedIn, isCustomer } = useAuth();
  const [cart, setCart]     = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchCart = useCallback(async () => {
    if (!isLoggedIn || !isCustomer) return;
    try {
      setLoading(true);
      const res = await cartAPI.get();
      setCart(res.data.data);
    } catch { /* silent */ }
    finally { setLoading(false); }
  }, [isLoggedIn, isCustomer]);

  useEffect(() => { fetchCart(); }, [fetchCart]);

  const addItem = async (productId, quantity = 1) => {
    const res = await cartAPI.addItem(productId, quantity);
    setCart(res.data.data);
    return res.data.data;
  };

  const updateItem = async (itemId, quantity) => {
    const res = await cartAPI.updateItem(itemId, quantity);
    setCart(res.data.data);
    return res.data.data;
  };

  const removeItem = async (itemId) => {
    const res = await cartAPI.removeItem(itemId);
    setCart(res.data.data);
  };

  const itemCount = cart?.items?.reduce((s, i) => s + i.quantity, 0) ?? 0;

  return (
    <CartContext.Provider value={{ cart, loading, fetchCart, addItem, updateItem, removeItem, itemCount }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
