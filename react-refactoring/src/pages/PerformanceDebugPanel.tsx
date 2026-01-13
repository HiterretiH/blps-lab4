import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { api } from '@/services/api';
import { 
  BarChart3, 
  Cpu, 
  Clock, 
  Activity, 
  RefreshCw, 
  AlertTriangle,
  Zap,
  Gauge,
  Download,
  Shield,
  Database,
  Layers,
  Trash2,
  History,
  Filter,
  Settings,
  Plus,
  Minus,
  Radio,
  Bell
} from 'lucide-react';

interface ApiMetric {
  url: string;
  method: string;
  duration: number;
  status: number;
  success: boolean;
  timestamp: string;
}

interface ApiStats {
  totalCalls: number;
  avgDuration: number;
  successRate: number;
  recentErrors: number;
  slowCalls: number;
  recentMetrics: ApiMetric[];
}

interface StorageData {
  state?: {
    metrics: ApiMetric[];
    statsLimit?: number;
    lastCleanup?: string;
  };
  version?: number;
  metrics?: ApiMetric[];
}

export const PerformanceDebugPanel: React.FC = () => {
  const [apiStats, setApiStats] = useState<ApiStats>({
    totalCalls: 0,
    avgDuration: 0,
    successRate: 100,
    recentErrors: 0,
    slowCalls: 0,
    recentMetrics: []
  });
  
  const [memory, setMemory] = useState<{
    used: number;
    total: number;
    limit: number;
  }>({ used: 0, total: 0, limit: 0 });
  
  const [fps, setFps] = useState(0);
  const [lastUpdate, setLastUpdate] = useState<string>(new Date().toLocaleTimeString());
  const [autoRefreshEnabled, setAutoRefreshEnabled] = useState(true);
  const [statsLimit, setStatsLimit] = useState(250);
  const [storageMetricsCount, setStorageMetricsCount] = useState(0);
  const [refreshInterval] = useState(3000);
  const [notificationCount, setNotificationCount] = useState(0);
  
  const prevStorageDataRef = useRef<string>('');
  const prevMetricsCountRef = useRef(0);

  const readMetricsFromStorage = useCallback((): ApiMetric[] => {
    try {
      const storageData = localStorage.getItem('api-metrics-storage');
      if (!storageData) return [];
      
      const parsed: StorageData = JSON.parse(storageData);
      
      const metrics = 
        parsed.state?.metrics || 
        parsed.metrics || 
        (parsed as any)?.state?.state?.metrics || 
        [];
      
      return Array.isArray(metrics) ? metrics : [];
    } catch (error) {
      console.error('❌ Ошибка чтения из localStorage:', error);
      return [];
    }
  }, []);

  const calculateStats = useCallback((metrics: ApiMetric[], limit: number): ApiStats => {
    const recentMetrics = metrics.slice(-limit);
    const totalCalls = recentMetrics.length;
    
    if (totalCalls === 0) {
      return {
        totalCalls: 0,
        avgDuration: 0,
        successRate: 100,
        recentErrors: 0,
        slowCalls: 0,
        recentMetrics: []
      };
    }
    
    const totalDuration = recentMetrics.reduce((sum, metric) => sum + (metric.duration || 0), 0);
    const avgDuration = Math.round(totalDuration / totalCalls);
    
    const successCount = recentMetrics.filter(m => m.success !== false).length;
    const successRate = Math.round((successCount / totalCalls) * 100);
    
    const recentErrors = recentMetrics.filter(m => m.success === false).length;
    const slowCalls = recentMetrics.filter(m => (m.duration || 0) > 1000).length;
    
    return {
      totalCalls,
      avgDuration,
      successRate,
      recentErrors,
      slowCalls,
      recentMetrics: recentMetrics.slice(-10)
    };
  }, []);

  const updateData = useCallback(() => {
    console.log('🔄 Обновление данных из localStorage');
    
    const metrics = readMetricsFromStorage();
    const currentCount = metrics.length;
    
    setStorageMetricsCount(currentCount);
    
    if (currentCount > prevMetricsCountRef.current) {
      const newCount = currentCount - prevMetricsCountRef.current;
      setNotificationCount(prev => prev + newCount);
      console.log(`🆕 Обнаружено новых метрик: +${newCount}`);
      prevMetricsCountRef.current = currentCount;
    }
    
    const newStats = calculateStats(metrics, statsLimit);
    setApiStats(newStats);
    setLastUpdate(new Date().toLocaleTimeString());
    
    console.log('📊 Обновленная статистика:', {
      totalCalls: newStats.totalCalls,
      storageCount: currentCount,
      displayed: newStats.recentMetrics.length
    });
  }, [readMetricsFromStorage, calculateStats, statsLimit]);

  useEffect(() => {
    if (!autoRefreshEnabled) return;
    
    updateData();
    
    const interval = setInterval(updateData, refreshInterval);
    return () => clearInterval(interval);
  }, [autoRefreshEnabled, refreshInterval, updateData]);

  useEffect(() => {
    const handleStorageChange = (event: StorageEvent) => {
      if (event.key === 'api-metrics-storage' && autoRefreshEnabled) {
        console.log('📡 Обнаружено изменение localStorage');
        
        setTimeout(() => {
          updateData();
        }, 100);
      }
    };
    
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [autoRefreshEnabled, updateData]);

  useEffect(() => {
    const interval = setInterval(() => {
      if ('memory' in performance) {
        const perfMemory = (performance as any).memory;
        setMemory({
          used: Math.round(perfMemory.usedJSHeapSize / (1024 * 1024)),
          total: Math.round(perfMemory.totalJSHeapSize / (1024 * 1024)),
          limit: Math.round(perfMemory.jsHeapSizeLimit / (1024 * 1024)),
        });
      }
    }, 5000);

    let frameCount = 0;
    let lastTime = performance.now();
    
    const checkFps = () => {
      frameCount++;
      const currentTime = performance.now();
      
      if (currentTime - lastTime >= 1000) {
        setFps(frameCount);
        frameCount = 0;
        lastTime = currentTime;
      }
      
      requestAnimationFrame(checkFps);
    };
    
    requestAnimationFrame(checkFps);

    return () => {
      clearInterval(interval);
    };
  }, []);

  const [webVitals, setWebVitals] = useState({
    cls: 0,
    fid: 0,
    lcp: 0,
    fcp: 0,
    ttfb: 0
  });

  useEffect(() => {
    const loadWebVitals = () => {
      try {
        const keys = ['web-vitals-history', 'web-vitals', 'web_vitals_history'];
        let vitalsData: any = {};
        
        for (const key of keys) {
          const data = localStorage.getItem(key);
          if (data) {
            vitalsData = JSON.parse(data);
            break;
          }
        }
        
        let fidValue = 0;
        if (vitalsData.INP && vitalsData.INP.length > 0) {
          const latestINP = vitalsData.INP[vitalsData.INP.length - 1];
          fidValue = latestINP.value || latestINP || 0;
        } else if (vitalsData.FID && vitalsData.FID.length > 0) {
          const latestFID = vitalsData.FID[vitalsData.FID.length - 1];
          fidValue = latestFID.value || latestFID || 0;
        }
        
        const getLatestValue = (key: string) => {
          const values = vitalsData[key];
          if (Array.isArray(values) && values.length > 0) {
            return values[values.length - 1]?.value || values[values.length - 1] || 0;
          }
          return 0;
        };
        
        setWebVitals({
          cls: getLatestValue('CLS') || getLatestValue('cls') || 0,
          fid: fidValue,
          lcp: getLatestValue('LCP') || getLatestValue('lcp') || 0,
          fcp: getLatestValue('FCP') || getLatestValue('fcp') || 0,
          ttfb: getLatestValue('TTFB') || getLatestValue('ttfb') || 0,
        });
      } catch (error) {
        console.error('Error loading Web Vitals:', error);
      }
    };
    
    loadWebVitals();
  }, []);

  const handleManualRefresh = () => {
    console.log('🔄 Ручное обновление');
    updateData();
  };

  const handleTestApiCall = async () => {
    console.log('🧪 Тестовый API вызов');
    try {
      await api.get('/applications');
      console.log('✅ API вызов выполнен');
      
      setTimeout(updateData, 300);
    } catch (error) {
      console.error('❌ Ошибка API:', error);
    }
  };

  const handleClearMetrics = () => {
    localStorage.removeItem('api-metrics-storage');
    localStorage.removeItem('web-vitals-history');
    localStorage.removeItem('web-vitals');
    localStorage.removeItem('web_vitals_history');
    
    setApiStats({
      totalCalls: 0,
      avgDuration: 0,
      successRate: 100,
      recentErrors: 0,
      slowCalls: 0,
      recentMetrics: []
    });
    
    setWebVitals({ cls: 0, fid: 0, lcp: 0, fcp: 0, ttfb: 0 });
    setNotificationCount(0);
    prevMetricsCountRef.current = 0;
    
    console.log('✅ Все метрики очищены');
  };

  const handleExportMetrics = () => {
    const metrics = readMetricsFromStorage();
    const data = {
      timestamp: new Date().toISOString(),
      totalMetrics: metrics.length,
      apiStats,
      metrics: metrics.slice(-100),
      memory,
      fps,
      webVitals,
      settings: {
        statsLimit,
        autoRefreshEnabled,
        refreshInterval
      }
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `performance-metrics-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const increaseStatsLimit = () => {
    const newLimit = Math.min(1000, statsLimit + 50);
    setStatsLimit(newLimit);
    updateData();
  };
  
  const decreaseStatsLimit = () => {
    const newLimit = Math.max(50, statsLimit - 50);
    setStatsLimit(newLimit);
    updateData();
  };
  
  const resetStatsLimit = () => {
    setStatsLimit(250);
    updateData();
  };

  const toggleAutoRefresh = () => {
    setAutoRefreshEnabled(!autoRefreshEnabled);
  };

  const clearNotifications = () => {
    setNotificationCount(0);
  };

  const formatVitalValue = (value: number, metric: string) => {
    if (metric === 'cls') return value.toFixed(3);
    if (metric === 'fid' || metric === 'lcp' || metric === 'fcp' || metric === 'ttfb') {
      return `${Math.round(value)}ms`;
    }
    return value.toFixed(2);
  };

  const getVitalStatus = (value: number, metric: string) => {
    const thresholds: Record<string, { good: number; needsImprovement: number }> = {
      cls: { good: 0.1, needsImprovement: 0.25 },
      fid: { good: 100, needsImprovement: 300 },
      lcp: { good: 2500, needsImprovement: 4000 },
      fcp: { good: 1800, needsImprovement: 3000 },
      ttfb: { good: 800, needsImprovement: 1800 }
    };
    
    const threshold = thresholds[metric];
    if (!threshold) return 'text-gray-600';
    
    if (value <= threshold.good) return 'text-green-600';
    if (value <= threshold.needsImprovement) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getVitalLabel = (metric: string) => {
    const labels: Record<string, string> = {
      cls: 'CLS',
      fid: 'FID',
      lcp: 'LCP',
      fcp: 'FCP',
      ttfb: 'TTFB'
    };
    return labels[metric] || metric.toUpperCase();
  };

  const getVitalDescription = (metric: string) => {
    const descriptions: Record<string, string> = {
      cls: 'Cumulative Layout Shift',
      fid: 'First Input Delay',
      lcp: 'Largest Contentful Paint',
      fcp: 'First Contentful Paint',
      ttfb: 'Time to First Byte'
    };
    return descriptions[metric] || metric;
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="flex items-center gap-2">
          <Activity className="h-5 w-5" />
          Performance Monitor
          {notificationCount > 0 && (
            <span className="ml-2 inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-800">
              <Bell className="h-3 w-3 mr-1" />
              +{notificationCount}
            </span>
          )}
        </CardTitle>
        <div className="flex gap-2">
          {/* Управление обновлением */}
          <div className="flex items-center gap-1 border rounded-md px-2 py-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={toggleAutoRefresh}
              title={autoRefreshEnabled ? "Выключить автообновление" : "Включить автообновление"}
              className={`h-6 w-6 p-0 ${autoRefreshEnabled ? 'text-green-600' : 'text-gray-400'}`}
            >
              <Radio className="h-3 w-3" />
            </Button>
            <span className="text-xs text-gray-500">
              {refreshInterval / 1000}с
            </span>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleManualRefresh}
              title="Обновить сейчас"
              className="h-6 w-6 p-0"
            >
              <RefreshCw className="h-3 w-3" />
            </Button>
            {notificationCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={clearNotifications}
                title="Очистить уведомления"
                className="h-6 w-6 p-0 text-red-600"
              >
                <Bell className="h-3 w-3" />
              </Button>
            )}
          </div>

          {/* Управление лимитом */}
          <div className="flex items-center gap-1 border rounded-md px-2 py-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={decreaseStatsLimit}
              title="Уменьшить лимит"
              className="h-6 w-6 p-0"
            >
              <Minus className="h-3 w-3" />
            </Button>
            <span className="text-sm font-medium px-2">
              Лимит: {statsLimit}
            </span>
            <Button
              variant="ghost"
              size="sm"
              onClick={increaseStatsLimit}
              title="Увеличить лимит"
              className="h-6 w-6 p-0"
            >
              <Plus className="h-3 w-3" />
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={resetStatsLimit}
              title="Сбросить до 250"
              className="h-6 w-6 p-0"
            >
              <Settings className="h-3 w-3" />
            </Button>
          </div>

          {/* Основные кнопки */}
          <Button 
            variant="outline" 
            size="sm" 
            onClick={handleClearMetrics}
            title="Очистить все метрики"
          >
            <Trash2 className="h-4 w-4 mr-2" />
            Очистить все
          </Button>
          
          <Button 
            variant="outline" 
            size="sm" 
            onClick={handleExportMetrics}
            title="Экспортировать метрики"
          >
            <Download className="h-4 w-4 mr-2" />
            Экспорт
          </Button>
          
          <Button 
            variant="outline" 
            size="sm" 
            onClick={handleTestApiCall}
            title="Тестовый API вызов"
          >
            <RefreshCw className="h-4 w-4 mr-2" />
            Тест API
          </Button>
        </div>
      </CardHeader>
      
      <CardContent>
        {/* Информация об обновлении */}
        <div className="mb-4 p-2 bg-gray-50 rounded-lg flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className={`w-3 h-3 rounded-full ${autoRefreshEnabled ? 'bg-green-500 animate-pulse' : 'bg-gray-300'}`} />
            <span className="text-sm text-gray-600">
              {autoRefreshEnabled ? 'Автообновление включено' : 'Автообновление выключено'}
            </span>
          </div>
          <div className="text-sm text-gray-500">
            Обновлено: {lastUpdate}
          </div>
        </div>

        <div className="space-y-6">
          {/* API Statistics */}
          <div>
            <h3 className="text-lg font-semibold mb-3 flex items-center gap-2">
              <BarChart3 className="h-5 w-5" />
              API Statistics
              <span className="text-sm font-normal text-gray-500">
                (показано последних {statsLimit > apiStats.totalCalls ? apiStats.totalCalls : statsLimit} из {storageMetricsCount} вызовов)
              </span>
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <BarChart3 className="h-4 w-4 text-blue-600" />
                  <span className="text-sm font-medium text-gray-600">API Calls</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">{apiStats.totalCalls}</p>
                <p className="text-xs text-gray-500 mt-1">всего в хранилище: {storageMetricsCount}</p>
              </div>
              
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <Clock className="h-4 w-4 text-green-600" />
                  <span className="text-sm font-medium text-gray-600">Avg Time</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">{apiStats.avgDuration}ms</p>
              </div>
              
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <Activity className="h-4 w-4 text-purple-600" />
                  <span className="text-sm font-medium text-gray-600">Success Rate</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">{apiStats.successRate}%</p>
              </div>
              
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <AlertTriangle className="h-4 w-4 text-orange-600" />
                  <span className="text-sm font-medium text-gray-600">Errors</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">{apiStats.recentErrors}</p>
              </div>
            </div>
          </div>

          {/* Metrics Storage */}
          <div className="mb-6">
            <h3 className="text-lg font-semibold mb-3 flex items-center gap-2">
              <Database className="h-5 w-5" />
              Metrics Storage
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <History className="h-4 w-4 text-blue-600" />
                  <span className="text-sm font-medium text-gray-600">Total Stored</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">{storageMetricsCount}</p>
                <p className="text-xs text-gray-500 mt-1">max: 1000</p>
              </div>
              
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <BarChart3 className="h-4 w-4 text-purple-600" />
                  <span className="text-sm font-medium text-gray-600">Showing</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">{apiStats.totalCalls}</p>
                <p className="text-xs text-gray-500 mt-1">limit: {statsLimit}</p>
              </div>
              
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <Clock className="h-4 w-4 text-green-600" />
                  <span className="text-sm font-medium text-gray-600">Storage Size</span>
                </div>
                <p className="text-2xl font-bold text-gray-900">
                  {Math.round(JSON.stringify(readMetricsFromStorage()).length / 1024)} KB
                </p>
              </div>
              
              <div className="bg-gray-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <Activity className="h-4 w-4 text-orange-600" />
                  <span className="text-sm font-medium text-gray-600">Auto Cleanup</span>
                </div>
                <p className="text-lg font-medium text-gray-900">Every 5 min</p>
                <p className="text-xs text-gray-500 mt-1">Metrics older than 1 hour</p>
              </div>
            </div>
          </div>

          {/* System Performance */}
          <div>
            <h3 className="text-lg font-semibold mb-3 flex items-center gap-2">
              <Cpu className="h-5 w-5" />
              System Performance
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
              <div className="bg-blue-50 rounded-lg p-3">
                <div className="flex items-center gap-2 mb-1">
                  <Zap className="h-4 w-4 text-blue-600" />
                  <span className="text-sm font-medium text-blue-600">FPS</span>
                </div>
                <p className={`text-2xl font-bold ${
                  fps > 50 ? 'text-green-600' : fps > 30 ? 'text-yellow-600' : 'text-red-600'
                }`}>
                  {fps}
                </p>
              </div>
              
              {memory.used > 0 && (
                <>
                  <div className="bg-green-50 rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <Database className="h-4 w-4 text-green-600" />
                      <span className="text-sm font-medium text-green-600">Memory Used</span>
                    </div>
                    <p className="text-2xl font-bold text-green-600">{memory.used} MB</p>
                    <p className="text-xs text-green-500 mt-1">{Math.round((memory.used / memory.limit) * 100)}% of limit</p>
                  </div>
                  
                  <div className="bg-purple-50 rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <Database className="h-4 w-4 text-purple-600" />
                      <span className="text-sm font-medium text-purple-600">Memory Limit</span>
                    </div>
                    <p className="text-2xl font-bold text-purple-600">{memory.limit} MB</p>
                  </div>
                </>
              )}
            </div>
          </div>

          {/* Web Vitals */}
          <div>
            <h3 className="text-lg font-semibold mb-3 flex items-center gap-2">
              <Gauge className="h-5 w-5" />
              Web Vitals (Core Web Vitals)
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
              {Object.entries(webVitals).map(([key, value]) => (
                <div key={key} className="bg-gradient-to-br from-gray-50 to-white rounded-lg p-3 border">
                  <div className="flex items-center gap-2 mb-1">
                    <div className={`p-1 rounded ${getVitalStatus(value, key).replace('text', 'bg').replace('-600', '-100')}`}>
                      <Activity className={`h-4 w-4 ${getVitalStatus(value, key)}`} />
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-600">
                        {getVitalLabel(key)}
                      </span>
                      <p className="text-xs text-gray-500">{getVitalDescription(key)}</p>
                    </div>
                  </div>
                  <p className={`text-xl font-bold mt-2 ${getVitalStatus(value, key)}`}>
                    {formatVitalValue(value, key)}
                  </p>
                  <div className="mt-2 w-full bg-gray-200 rounded-full h-1.5">
                    <div 
                      className={`h-1.5 rounded-full ${
                        getVitalStatus(value, key) === 'text-green-600' ? 'bg-green-500' :
                        getVitalStatus(value, key) === 'text-yellow-600' ? 'bg-yellow-500' : 'bg-red-500'
                      }`}
                      style={{ 
                        width: `${Math.min(value * 100, 100)}%`,
                        maxWidth: '100%'
                      }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Recent API Calls */}
          <div>
            <h3 className="text-lg font-semibold mb-3">Recent API Calls</h3>
            {apiStats.recentMetrics.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-gray-50">
                      <th className="p-2 text-left">Method</th>
                      <th className="p-2 text-left">URL</th>
                      <th className="p-2 text-left">Duration</th>
                      <th className="p-2 text-left">Status</th>
                      <th className="p-2 text-left">Time</th>
                    </tr>
                  </thead>
                  <tbody>
                    {apiStats.recentMetrics.map((metric, i) => (
                      <tr key={i} className="border-b hover:bg-gray-50">
                        <td className="p-2">
                          <span className={`px-2 py-1 rounded text-xs ${
                            metric.method === 'GET' ? 'bg-blue-100 text-blue-800' :
                            metric.method === 'POST' ? 'bg-green-100 text-green-800' :
                            'bg-gray-100 text-gray-800'
                          }`}>
                            {metric.method}
                          </span>
                        </td>
                        <td className="p-2 font-mono text-xs truncate max-w-[200px]">
                          {metric.url}
                        </td>
                        <td className={`p-2 font-medium ${
                          metric.duration > 1000 ? 'text-red-600' :
                          metric.duration > 500 ? 'text-yellow-600' : 'text-green-600'
                        }`}>
                          {metric.duration}ms
                        </td>
                        <td className="p-2">
                          <span className={`px-2 py-1 rounded text-xs ${
                            metric.status >= 200 && metric.status < 300 ? 'bg-green-100 text-green-800' :
                            metric.status >= 400 ? 'bg-red-100 text-red-800' :
                            'bg-yellow-100 text-yellow-800'
                          }`}>
                            {metric.status}
                          </span>
                        </td>
                        <td className="p-2 text-xs text-gray-500">
                          {metric.timestamp ? new Date(metric.timestamp).toLocaleTimeString() : 'N/A'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-4 text-gray-500">
                Нет данных о вызовах API
              </div>
            )}
          </div>
        </div>
        
        <div className="mt-6 flex gap-2">
          <Button variant="outline" size="sm" onClick={handleTestApiCall}>
            Test API Call
          </Button>
          <Button variant="outline" size="sm" onClick={handleManualRefresh}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh Now
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};