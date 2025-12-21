import React from 'react';
import { Application } from '../../types';
import { cn, formatCurrency } from '../../utils';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { Download, Eye, Trash2, Edit3, BarChart3 } from 'lucide-react';

interface AppCardProps {
  application: Application;
  onSelect?: (app: Application) => void;
  onDelete?: (id: number) => void;
  onEdit?: (app: Application) => void;
  currentDeveloperId?: number;
}

export const AppCard: React.FC<AppCardProps> = ({
  application,
  onSelect,
  onDelete,
  onEdit,
  currentDeveloperId,
}) => {
  // Проверяем владельца приложения
  const isOwner = currentDeveloperId ? application.developerId === currentDeveloperId : false;

  // Статус приложения
  const getStatusInfo = (status: number) => {
    switch (status) {
      case 0: // PENDING
        return {
          variant: 'warning' as const,
          label: 'НА РАССМОТРЕНИИ',
          text: 'Ожидает проверки администратором',
          icon: '⏳',
        };
      case 1: // ACCEPTED
        return {
          variant: 'success' as const,
          label: 'ОДОБРЕНО',
          text: 'Приложение доступно для скачивания',
          icon: '✅',
        };
      case 2: // REJECTED
        return {
          variant: 'danger' as const,
          label: 'ОТКЛОНЕНО',
          text: 'Приложение не прошло проверку',
          icon: '❌',
        };
      default:
        return {
          variant: 'default' as const,
          label: 'НЕИЗВЕСТНО',
          text: 'Статус не определен',
          icon: '❓',
        };
    }
  };

  // Цвет для типа приложения
  const getTypeInfo = (type: string) => {
    const types: Record<string, { color: string; icon: string; label: string }> = {
      GAME: { color: 'bg-purple-100 text-purple-800', icon: '🎮', label: 'Игра' },
      MUSIC: { color: 'bg-pink-100 text-pink-800', icon: '🎵', label: 'Музыка' },
      HEALTH: { color: 'bg-green-100 text-green-800', icon: '🏥', label: 'Здоровье' },
      SOCIAL: { color: 'bg-blue-100 text-blue-800', icon: '👥', label: 'Социальное' },
      EDUCATION: { color: 'bg-yellow-100 text-yellow-800', icon: '📚', label: 'Образование' },
      FINANCE: { color: 'bg-indigo-100 text-indigo-800', icon: '💰', label: 'Финансы' },
    };

    return types[type] || { color: 'bg-gray-100 text-gray-800', icon: '📱', label: type };
  };

  const {
    variant: statusVariant,
    label: statusLabel,
    text: statusText,
    icon: statusIcon,
  } = getStatusInfo(application.status);
  const { color: typeColor, icon: typeIcon, label: typeLabel } = getTypeInfo(application.type);
  const canDownload = application.status === 1; // Только APPROVED можно скачать

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white transition-all duration-300 hover:shadow-lg">
      {/* Заголовок */}
      <div className="border-b border-gray-200 bg-gradient-to-r from-gray-50 to-white px-6 py-4">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="mb-1 flex items-center gap-2">
              <span className="text-lg">{typeIcon}</span>
              <h3 className="truncate text-lg font-semibold text-gray-900">{application.name}</h3>
            </div>

            <div className="mt-2 flex flex-wrap items-center gap-2">
              <span className={cn('rounded-full px-2 py-1 text-xs font-medium', typeColor)}>
                {typeLabel}
              </span>

              <Badge variant={statusVariant} className="flex items-center gap-1">
                <span>{statusIcon}</span>
                <span>{statusLabel}</span>
              </Badge>

              {isOwner && (
                <span className="rounded-full bg-primary-100 px-2 py-1 text-xs font-medium text-primary-800">
                  🧑‍💻 Ваше
                </span>
              )}
            </div>
          </div>

          <div className="text-right">
            <p className="text-2xl font-bold text-primary-600">
              {formatCurrency(application.price)}
            </p>
            <p className="text-xs text-gray-500">ID: #{application.id}</p>
          </div>
        </div>
      </div>

      {/* Описание */}
      <div className="px-6 py-4">
        <p className="mb-4 line-clamp-3 text-gray-700">{application.description}</p>

        <div className="flex flex-col gap-1 text-sm text-gray-500">
          <div className="flex items-center gap-2">
            <span className="font-medium">Разработчик ID:</span>
            <span className="font-semibold">{application.developerId}</span>
            {isOwner && <span className="text-xs text-green-600">(Это вы)</span>}
          </div>

          {application.createdAt && (
            <div className="flex items-center gap-2">
              <span className="font-medium">Создано:</span>
              <span>{new Date(application.createdAt).toLocaleDateString('ru-RU')}</span>
            </div>
          )}
        </div>
      </div>

      {/* Действия */}
      <div className="border-t border-gray-200 bg-gray-50 px-6 py-4">
        <div className="flex flex-wrap gap-2">
          {/* Основные действия */}
          <div className="flex flex-1 flex-wrap gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => onSelect?.(application)}
              className="flex items-center gap-2"
            >
              <Eye className="h-3 w-3" />
              Просмотр
            </Button>

            {canDownload && (
              <Button variant="primary" size="sm" className="flex items-center gap-2">
                <Download className="h-3 w-3" />
                Скачать
              </Button>
            )}

            {isOwner && application.status === 1 && (
              <Button variant="outline" size="sm" className="flex items-center gap-2">
                <BarChart3 className="h-3 w-3" />
                Статистика
              </Button>
            )}
          </div>

          {/* Действия владельца */}
          {isOwner && (
            <div className="flex gap-2">
              {onEdit && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onEdit(application)}
                  className="flex items-center gap-2"
                >
                  <Edit3 className="h-3 w-3" />
                  Изменить
                </Button>
              )}

              {onDelete && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onDelete(application.id)}
                  className="flex items-center gap-2 border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                >
                  <Trash2 className="h-3 w-3" />
                  Удалить
                </Button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
