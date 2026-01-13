import { useState, useCallback, useRef } from 'react';
import { adsService } from '@/services/ads.service';
import { InAppAdd } from '@/types';
import { useToast } from '@/hooks/useToast';

export const useInAppAdds = (monetizedAppId?: number) => {
  const [ads, setAds] = useState<InAppAdd[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { toast } = useToast();
  
  const isFetching = useRef(false);
  const abortController = useRef<AbortController | null>(null);
  const lastFetchedAppId = useRef<number | undefined>();

  const fetchAds = useCallback(async (force = false) => {
    if (!monetizedAppId) {
      console.log('No monetizedAppId provided, skipping ads fetch');
      return;
    }
    
    if (isFetching.current && !force && lastFetchedAppId.current === monetizedAppId) {
      console.log('Already fetching ads for this app, skipping');
      return;
    }
    
    if (abortController.current) {
      abortController.current.abort();
    }
    
    abortController.current = new AbortController();
    isFetching.current = true;
    lastFetchedAppId.current = monetizedAppId;
    setIsLoading(true);
    setError(null);
    
    try {
      console.log(`Fetching ads for monetized app ID: ${monetizedAppId}`);
      const adsData = await adsService.getAdsByMonetizedApp(monetizedAppId);
      console.log(`Found ${adsData.length} ads for monetized app ${monetizedAppId}`);
      
      setAds(adsData);
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log('Ads fetch cancelled');
        return;
      }
      
      setError(err.message);
      toast.error('Failed to load ads');
      console.error(`Error fetching ads for app ${monetizedAppId}:`, err);
    } finally {
      setIsLoading(false);
      isFetching.current = false;
    }
  }, [monetizedAppId, toast]);

  const createAd = useCallback(async (data: {
    title: string;
    description?: string;
    price: number;
    monetizedApplicationId: number;
  }) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const newAd = await adsService.createAd(data);
      
      setAds(prev => [...prev, newAd]);
      toast.success('Ad created successfully');
      return newAd;
    } catch (err: any) {
      setError(err.message);
      toast.error(err.message || 'Failed to create ad');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [toast]);

  const updateAd = useCallback(async (id: number, data: {
  title?: string;
  description?: string;
  price?: number;
  monetizedApplicationId?: number;
}) => {
  setIsLoading(true);
  setError(null);
  
  try {
    const response = await fetch(
      `http://localhost:727/api/in-app-ads/${id}`,
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
      throw new Error(errorData.message || 'Failed to update ad');
    }
    
    const updatedAd = await response.json();
    
    setAds(prev => prev.map(ad => 
      ad.id === id ? updatedAd : ad
    ));
    
    toast.success('Ad updated successfully');
    return updatedAd;
  } catch (err: any) {
    setError(err.message);
    toast.error(err.message || 'Failed to update ad');
    throw err;
  } finally {
    setIsLoading(false);
  }
}, [toast]);

  const deleteAd = useCallback(async (id: number) => {
    setIsLoading(true);
    setError(null);
    
    try {
      await adsService.deleteAd(id);
      
      setAds(prev => prev.filter(ad => ad.id !== id));
      toast.success('Ad deleted successfully');
    } catch (err: any) {
      setError(err.message);
      toast.error(err.message || 'Failed to delete ad');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [toast]);

  return {
    ads,
    isLoading,
    error,
    fetchAds,
    createAd,
    updateAd,
    deleteAd,
  };
};