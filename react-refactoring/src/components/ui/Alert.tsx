import React from 'react';
import { cn } from '../../utils';
import { AlertCircle, CheckCircle, Info, XCircle } from 'lucide-react';

interface AlertProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'info' | 'success' | 'warning' | 'danger';
  title?: string;
  children: React.ReactNode;
}

export const Alert: React.FC<AlertProps> = ({
  variant = 'info',
  title,
  children,
  className,
  ...props
}) => {
  const icons = {
    info: <Info className="h-5 w-5 text-blue-400" />,
    success: <CheckCircle className="h-5 w-5 text-green-400" />,
    warning: <AlertCircle className="h-5 w-5 text-yellow-400" />,
    danger: <XCircle className="h-5 w-5 text-red-400" />,
  };

  const backgrounds = {
    info: 'bg-blue-50 border-blue-200',
    success: 'bg-green-50 border-green-200',
    warning: 'bg-yellow-50 border-yellow-200',
    danger: 'bg-red-50 border-red-200',
  };

  const textColors = {
    info: 'text-blue-800',
    success: 'text-green-800',
    warning: 'text-yellow-800',
    danger: 'text-red-800',
  };

  return (
    <div
      className={cn('rounded-lg border p-4', backgrounds[variant], className)}
      role="alert"
      {...props}
    >
      <div className="flex">
        <div className="mr-3 flex-shrink-0">{icons[variant]}</div>
        <div className="flex-1">
          {title && <h3 className={cn('font-medium', textColors[variant])}>{title}</h3>}
          <div className={cn('mt-1 text-sm', textColors[variant])}>{children}</div>
        </div>
      </div>
    </div>
  );
};
