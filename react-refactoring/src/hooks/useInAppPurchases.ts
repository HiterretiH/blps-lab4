import { useState, useCallback, useRef } from 'react';
import { purchasesService } from '@/services/purchases.service';
import { InAppPurchase } from '@/types';
import { useToast } from '@/hooks/useToast';

export const useInAppPurchases = () => {
  const [purchases, setPurchases] = useState<InAppPurchase[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { toast } = useToast();
  
  const isFetching = useRef(false);
  const abortController = useRef<AbortController | null>(null);

  const fetchPurchases = useCallback(async (force = false) => {
    if (isFetching.current && !force) {
      return;
    }
    
    if (abortController.current) {
      abortController.current.abort();
    }
    
    abortController.current = new AbortController();
    isFetching.current = true;
    setIsLoading(true);
    setError(null);
    
    try {
      console.log('Fetching in-app purchases...');
      const purchasesData = await purchasesService.getAllPurchases();
      console.log(`Found ${purchasesData.length} purchases`);
      
      setPurchases(purchasesData);
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log('Purchase fetch cancelled');
        return;
      }
      
      setError(err.message);
      toast.error('Failed to load purchases');
      console.error('Error fetching purchases:', err);
    } finally {
      setIsLoading(false);
      isFetching.current = false;
    }
  }, [toast]);

  const createPurchase = useCallback(async (data: {
    title: string;
    description?: string;
    price: number;
    monetizedApplicationId?: number;
  }) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch(
        'http://localhost:727/api/in-app-purchases/create-single',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to create purchase');
      }
      
      const newPurchase = await response.json();
      
      setPurchases(prev => [...prev, newPurchase]);
      toast.success('Purchase created successfully');
      return newPurchase;
    } catch (err: any) {
      setError(err.message);
      toast.error(err.message || 'Failed to create purchase');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [toast]);

  const updatePurchase = useCallback(async (id: number, data: {
    title?: string;
    description?: string;
    price?: number;
  }) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch(
        `http://localhost:727/api/in-app-purchases/${id}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
          },
          body: JSON.stringify(data),
        }
      );
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update purchase');
      }
      
      const updatedPurchase = await response.json();
      
      setPurchases(prev => prev.map(p => 
        p.id === id ? updatedPurchase : p
      ));
      
      toast.success('Purchase updated successfully');
      return updatedPurchase;
    } catch (err: any) {
      setError(err.message);
      toast.error(err.message || 'Failed to update purchase');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [toast]);

  const deletePurchase = useCallback(async (id: number) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await fetch(
        `http://localhost:727/api/in-app-purchases/${id}`,
        {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
          },
        }
      );
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to delete purchase');
      }
      
      setPurchases(prev => prev.filter(p => p.id !== id));
      toast.success('Purchase deleted successfully');
    } catch (err: any) {
      setError(err.message);
      toast.error(err.message || 'Failed to delete purchase');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [toast]);

  return {
    purchases,
    isLoading,
    error,
    fetchPurchases,
    createPurchase,
    updatePurchase,
    deletePurchase,
  };
};