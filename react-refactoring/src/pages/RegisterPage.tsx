import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { authService } from '../services/auth.service';
import { RegisterCredentials } from '../types';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState<RegisterCredentials>({
    username: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isDeveloper, setIsDeveloper] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = isDeveloper
        ? await authService.registerDeveloper(credentials)
        : await authService.register(credentials);

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
      setError('Registration failed. Please try again.');
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
        <h1 className="mb-6 text-2xl font-bold text-gray-900">Register</h1>

        <div className="mb-6">
          <div className="flex space-x-4">
            <button
              type="button"
              onClick={() => setIsDeveloper(false)}
              className={`flex-1 rounded-lg py-2 text-center ${!isDeveloper ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700'}`}
            >
              Regular User
            </button>
            <button
              type="button"
              onClick={() => setIsDeveloper(true)}
              className={`flex-1 rounded-lg py-2 text-center ${isDeveloper ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700'}`}
            >
              Developer
            </button>
          </div>
        </div>

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
            label="Email"
            name="email"
            type="email"
            value={credentials.email}
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
            Register as {isDeveloper ? 'Developer' : 'User'}
          </Button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 hover:text-primary-700">
              Login here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
