import React, { useState } from 'react';
import { Card, CardHeader, CardContent, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { Select } from '../components/ui/Select';
import { Tabs, TabPanel } from '../components/ui/Tabs';
import {
  Home,
  Package,
  BarChart3,
  Shield,
  User,
  Settings,
  Download,
  Eye,
  Trash2,
  Star,
  CheckCircle,
  AlertCircle,
} from 'lucide-react';

export const UiKitDemoPage: React.FC = () => {
  const [modalOpen, setModalOpen] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [selectValue, setSelectValue] = useState('option1');

  const selectOptions = [
    { value: 'option1', label: 'Option 1' },
    { value: 'option2', label: 'Option 2' },
    { value: 'option3', label: 'Option 3' },
  ];

  const tabs = [
    { id: 'buttons', label: 'Buttons' },
    { id: 'inputs', label: 'Inputs' },
    { id: 'cards', label: 'Cards' },
    { id: 'alerts', label: 'Alerts' },
    { id: 'badges', label: 'Badges' },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="mb-2 text-3xl font-bold text-gray-900">UI Kit Demo</h1>
        <p className="text-gray-600">Демонстрация всех компонентов из нашего UI-кита</p>
      </div>

      {/* Tabs для навигации */}
      <Tabs tabs={tabs}>
        {/* Buttons Tab */}
        <TabPanel id="buttons">
          <Card>
            <CardHeader>
              <CardTitle>Кнопки (Button)</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">Варианты</h3>
                  <div className="flex flex-wrap gap-3">
                    <Button variant="primary">Primary</Button>
                    <Button variant="secondary">Secondary</Button>
                    <Button variant="outline">Outline</Button>
                  </div>
                </div>

                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">Размеры</h3>
                  <div className="flex items-center gap-3">
                    <Button size="sm">Small</Button>
                    <Button size="md">Medium</Button>
                    <Button size="lg">Large</Button>
                  </div>
                </div>

                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">С иконками</h3>
                  <div className="flex flex-wrap gap-3">
                    <Button variant="primary" className="flex items-center gap-2">
                      <Download className="h-4 w-4" />
                      Download
                    </Button>
                    <Button variant="outline" className="flex items-center gap-2">
                      <Eye className="h-4 w-4" />
                      View Details
                    </Button>
                    <Button variant="secondary" className="flex items-center gap-2">
                      <Trash2 className="h-4 w-4" />
                      Delete
                    </Button>
                  </div>
                </div>

                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">Состояния</h3>
                  <div className="flex flex-wrap gap-3">
                    <Button isLoading>Loading</Button>
                    <Button disabled>Disabled</Button>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabPanel>

        {/* Inputs Tab */}
        <TabPanel id="inputs">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Поля ввода (Input)</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Input
                  label="Обычное поле"
                  placeholder="Введите текст..."
                  value={inputValue}
                  onChange={e => setInputValue(e.target.value)}
                />

                <Input
                  label="Поле с ошибкой"
                  placeholder="Неверный ввод..."
                  error="Это поле обязательно для заполнения"
                />

                <Input label="Поле пароля" type="password" placeholder="Введите пароль..." />

                <Input
                  label="Отключенное поле"
                  placeholder="Недоступно для редактирования"
                  disabled
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Выпадающие списки (Select)</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Select
                  label="Выберите опцию"
                  options={selectOptions}
                  value={selectValue}
                  onChange={e => setSelectValue(e.target.value)}
                />

                <Select
                  label="С ошибкой"
                  options={selectOptions}
                  error="Необходимо выбрать значение"
                />

                <Select label="Отключенный" options={selectOptions} disabled />
              </CardContent>
            </Card>
          </div>
        </TabPanel>

        {/* Cards Tab */}
        <TabPanel id="cards">
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle>Простая карточка</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600">
                  Это базовая карточка с заголовком и содержимым. Карточки используются для
                  группировки связанной информации.
                </p>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-primary-50 to-white">
              <CardHeader>
                <CardTitle className="text-primary-700">Цветная карточка</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-700">
                  Карточка с градиентным фоном для выделения важной информации.
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>С действиями</CardTitle>
                <Button variant="outline" size="sm">
                  Действие
                </Button>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-gray-600">Карточка с кнопкой действий в заголовке.</p>
                <div className="flex gap-2">
                  <Button size="sm" variant="primary">
                    Сохранить
                  </Button>
                  <Button size="sm" variant="outline">
                    Отмена
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabPanel>

        {/* Alerts Tab */}
        <TabPanel id="alerts">
          <div className="space-y-4">
            <Alert variant="info" title="Информационное сообщение">
              Это информационное сообщение для пользователя. Используется для предоставления
              дополнительной информации.
            </Alert>

            <Alert variant="success" title="Успешная операция">
              Операция выполнена успешно! Все изменения сохранены.
            </Alert>

            <Alert variant="warning" title="Предупреждение">
              Пожалуйста, проверьте введенные данные перед сохранением.
            </Alert>

            <Alert variant="danger" title="Ошибка">
              Произошла ошибка при обработке запроса. Пожалуйста, попробуйте еще раз.
            </Alert>

            <Alert variant="info">Сообщение без заголовка, только с текстом содержимого.</Alert>
          </div>
        </TabPanel>

        {/* Badges Tab */}
        <TabPanel id="badges">
          <Card>
            <CardHeader>
              <CardTitle>Бейджи (Badge)</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">Варианты</h3>
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="default">Default</Badge>
                    <Badge variant="primary">Primary</Badge>
                    <Badge variant="success">Success</Badge>
                    <Badge variant="warning">Warning</Badge>
                    <Badge variant="danger">Danger</Badge>
                    <Badge variant="info">Info</Badge>
                  </div>
                </div>

                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">С иконками</h3>
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="primary" className="flex items-center gap-1">
                      <Star className="h-3 w-3" />
                      Featured
                    </Badge>
                    <Badge variant="success" className="flex items-center gap-1">
                      <CheckCircle className="h-3 w-3" />
                      Verified
                    </Badge>
                    <Badge variant="warning" className="flex items-center gap-1">
                      <AlertCircle className="h-3 w-3" />
                      Pending
                    </Badge>
                  </div>
                </div>

                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">Статусы приложений</h3>
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="success">ACCEPTED</Badge>
                    <Badge variant="warning">PENDING</Badge>
                    <Badge variant="danger">REJECTED</Badge>
                  </div>
                </div>

                <div>
                  <h3 className="mb-3 text-lg font-medium text-gray-900">Роли пользователей</h3>
                  <div className="flex flex-wrap gap-2">
                    <Badge variant="info">USER</Badge>
                    <Badge variant="primary">DEVELOPER</Badge>
                    <Badge variant="danger">PRIVACY POLICY</Badge>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabPanel>
      </Tabs>

      {/* Модальное окно */}
      <Card>
        <CardHeader>
          <CardTitle>Модальное окно (Modal)</CardTitle>
        </CardHeader>
        <CardContent>
          <Button onClick={() => setModalOpen(true)}>Открыть модальное окно</Button>

          <Modal
            isOpen={modalOpen}
            onClose={() => setModalOpen(false)}
            title="Пример модального окна"
            size="lg"
          >
            <div className="space-y-4">
              <p className="text-gray-600">
                Это демонстрация модального окна. Оно включает в себя:
              </p>
              <ul className="list-disc space-y-1 pl-5 text-gray-600">
                <li>Заголовок с кнопкой закрытия</li>
                <li>Затемненный фон при открытии</li>
                <li>Закрытие по нажатию Escape</li>
                <li>Закрытие при клике вне окна</li>
                <li>Три размера: sm, md, lg</li>
              </ul>
              <div className="flex justify-end gap-3 pt-4">
                <Button variant="outline" onClick={() => setModalOpen(false)}>
                  Отмена
                </Button>
                <Button variant="primary" onClick={() => setModalOpen(false)}>
                  Подтвердить
                </Button>
              </div>
            </div>
          </Modal>
        </CardContent>
      </Card>

      {/* Пример комбинированного использования */}
      <Card>
        <CardHeader>
          <CardTitle>Комбинированный пример</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-medium text-gray-900">Заявка на верификацию</h3>
                <div className="mt-1 flex items-center gap-2">
                  <Badge variant="warning">PENDING</Badge>
                  <span className="text-sm text-gray-500">ID: #12345</span>
                </div>
              </div>
              <div className="flex gap-2">
                <Button variant="outline" size="sm">
                  <Eye className="mr-2 h-4 w-4" />
                  Просмотр
                </Button>
                <Button variant="primary" size="sm">
                  <CheckCircle className="mr-2 h-4 w-4" />
                  Одобрить
                </Button>
              </div>
            </div>

            <Alert variant="info">
              Эта заявка ожидает проверки администратора. Пожалуйста, рассмотрите ее в течение 24
              часов.
            </Alert>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <Input label="Комментарий" placeholder="Введите комментарий к решению..." />
              <Select
                label="Решение"
                options={[
                  { value: 'approve', label: 'Одобрить' },
                  { value: 'reject', label: 'Отклонить' },
                  { value: 'request_info', label: 'Запросить информацию' },
                ]}
              />
            </div>

            <div className="flex justify-end gap-3">
              <Button variant="outline">Отмена</Button>
              <Button variant="primary">Сохранить решение</Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Иконки Lucide React */}
      <Card>
        <CardHeader>
          <CardTitle>Иконки Lucide React</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4 md:grid-cols-6">
            <div className="flex flex-col items-center rounded-lg bg-gray-50 p-3">
              <Home className="mb-2 h-6 w-6 text-gray-600" />
              <span className="text-xs text-gray-600">Home</span>
            </div>
            <div className="flex flex-col items-center rounded-lg bg-gray-50 p-3">
              <Package className="mb-2 h-6 w-6 text-gray-600" />
              <span className="text-xs text-gray-600">Package</span>
            </div>
            <div className="flex flex-col items-center rounded-lg bg-gray-50 p-3">
              <BarChart3 className="mb-2 h-6 w-6 text-gray-600" />
              <span className="text-xs text-gray-600">Analytics</span>
            </div>
            <div className="flex flex-col items-center rounded-lg bg-gray-50 p-3">
              <Shield className="mb-2 h-6 w-6 text-gray-600" />
              <span className="text-xs text-gray-600">Shield</span>
            </div>
            <div className="flex flex-col items-center rounded-lg bg-gray-50 p-3">
              <User className="mb-2 h-6 w-6 text-gray-600" />
              <span className="text-xs text-gray-600">User</span>
            </div>
            <div className="flex flex-col items-center rounded-lg bg-gray-50 p-3">
              <Settings className="mb-2 h-6 w-6 text-gray-600" />
              <span className="text-xs text-gray-600">Settings</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
