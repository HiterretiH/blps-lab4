import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { authService } from '../services/auth.service';
import { LoginCredentials } from '../types';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState<LoginCredentials>({
    username: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await authService.login(credentials);
      localStorage.setItem('auth_token', response.token);
      localStorage.setItem(
        'user',
        JSON.stringify({
          username: credentials.username,
          role: response.role,
        })
      );
      navigate('/dashboard');
    } catch (err) {
      setError('Invalid username or password');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="mx-auto max-w-md">
      <div className="rounded-lg bg-white p-8 shadow">
        <h1 className="mb-6 text-2xl font-bold text-gray-900">Login</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Username"
            name="username"
            value={credentials.username}
            onChange={handleChange}
            required
            disabled={isLoading}
          />

          <Input
            label="Password"
            name="password"
            type="password"
            value={credentials.password}
            onChange={handleChange}
            required
            disabled={isLoading}
          />

          {error && <div className="rounded-lg bg-red-100 p-3 text-sm text-red-700">{error}</div>}

          <Button type="submit" className="w-full" isLoading={isLoading} disabled={isLoading}>
            Login
          </Button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary-600 hover:text-primary-700">
              Register here
            </Link>
          </p>
          <p className="mt-2 text-sm text-gray-600">
            Developer?{' '}
            <Link to="/register" className="text-primary-600 hover:text-primary-700">
              Register as developer
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
