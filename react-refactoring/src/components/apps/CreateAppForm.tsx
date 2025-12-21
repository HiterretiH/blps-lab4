import React, { useState } from 'react';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Modal } from '../ui/Modal';
import { Select } from '../ui/Select';
import { Alert } from '../ui/Alert';
import { Package } from 'lucide-react';

interface CreateAppFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: any) => Promise<void>;
  developerId: number | null;
}

const APP_TYPES = [
  { value: 'GAME', label: '🎮 Игра' },
  { value: 'MUSIC', label: '🎵 Музыка' },
  { value: 'HEALTH', label: '🏥 Здоровье' },
  { value: 'SOCIAL', label: '👥 Социальное' },
  { value: 'EDUCATION', label: '📚 Образование' },
  { value: 'FINANCE', label: '💰 Финансы' },
];

export const CreateAppForm: React.FC<CreateAppFormProps> = ({
  isOpen,
  onClose,
  onSubmit,
  developerId,
}) => {
  const [formData, setFormData] = useState({
    name: '',
    type: 'GAME',
    price: '0',
    description: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      if (!developerId) {
        throw new Error('Не удалось определить ID разработчика. Пожалуйста, войдите заново.');
      }

      // Валидация
      if (!formData.name.trim()) {
        throw new Error('Название приложения обязательно');
      }
      if (formData.name.trim().length < 3) {
        throw new Error('Название должно быть не менее 3 символов');
      }
      if (!formData.description.trim()) {
        throw new Error('Описание приложения обязательно');
      }
      if (formData.description.trim().length < 10) {
        throw new Error('Описание должно быть не менее 10 символов');
      }

      const price = parseFloat(formData.price);
      if (isNaN(price) || price < 0) {
        throw new Error('Цена должна быть положительным числом');
      }

      await onSubmit({
        name: formData.name.trim(),
        type: formData.type,
        price: price,
        description: formData.description.trim(),
        status: 0, // PENDING
        developerId: developerId,
      });

      // Сброс формы
      setFormData({
        name: '',
        type: 'GAME',
        price: '0',
        description: '',
      });
      onClose();
    } catch (err: any) {
      setError(err.message || 'Ошибка при создании приложения');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Создать новое приложение" size="lg">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Информация о разработчике */}
        {developerId && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-3">
            <div className="flex items-center gap-2">
              <Package className="h-4 w-4 text-blue-600" />
              <span className="text-sm font-medium text-blue-800">
                Создается от имени разработчика ID: {developerId}
              </span>
            </div>
          </div>
        )}

        {error && (
          <Alert variant="danger" title="Ошибка">
            {error}
          </Alert>
        )}

        <Input
          label="Название приложения"
          name="name"
          value={formData.name}
          onChange={handleChange}
          required
          placeholder="Например: Моя крутая игра"
          disabled={isLoading}
          maxLength={100}
        />

        <Select
          label="Тип приложения"
          name="type"
          value={formData.type}
          onChange={handleChange}
          options={APP_TYPES}
          disabled={isLoading}
        />

        <Input
          label="Цена (USD)"
          name="price"
          type="number"
          min="0"
          step="0.01"
          value={formData.price}
          onChange={handleChange}
          required
          disabled={isLoading}
          placeholder="0.00"
        />

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">
            Описание приложения
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            rows={4}
            className="w-full resize-none rounded-lg border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 disabled:cursor-not-allowed disabled:opacity-50"
            placeholder="Опишите ваше приложение, его функции и особенности..."
            disabled={isLoading}
            maxLength={500}
          />
          <div className="mt-1 flex justify-between">
            <p className="text-sm text-gray-500">
              Подробное описание поможет пользователям понять возможности вашего приложения
            </p>
            <p className="text-sm text-gray-500">{formData.description.length}/500</p>
          </div>
        </div>

        <div className="flex justify-end space-x-3 border-t pt-4">
          <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
            Отмена
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={isLoading}
            disabled={isLoading || !developerId}
            className="flex items-center gap-2"
          >
            <Package className="h-4 w-4" />
            Создать приложение
          </Button>
        </div>
      </form>
    </Modal>
  );
};
