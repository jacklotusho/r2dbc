import { TouchableOpacity, Text, ActivityIndicator, TouchableOpacityProps } from 'react-native';

interface ButtonProps extends TouchableOpacityProps {
  title: string;
  variant?: 'primary' | 'secondary' | 'outline' | 'danger';
  isLoading?: boolean;
  className?: string;
}

export function Button({
  title,
  variant = 'primary',
  isLoading = false,
  disabled,
  className = '',
  ...props
}: ButtonProps) {
  const baseClasses = 'py-4 px-6 rounded-xl items-center justify-center shadow-lg';
  
  const variantClasses = {
    primary: 'bg-blue-600 active:bg-blue-700',
    secondary: 'bg-gray-600 active:bg-gray-700',
    outline: 'bg-white border-2 border-blue-600 active:bg-blue-50',
    danger: 'bg-red-600 active:bg-red-700',
  };

  const textClasses = {
    primary: 'text-white font-bold text-lg',
    secondary: 'text-white font-bold text-lg',
    outline: 'text-blue-600 font-bold text-lg',
    danger: 'text-white font-bold text-lg',
  };

  const disabledClass = (disabled || isLoading) ? 'opacity-50' : '';

  return (
    <TouchableOpacity
      className={`${baseClasses} ${variantClasses[variant]} ${disabledClass} ${className}`}
      disabled={disabled || isLoading}
      activeOpacity={0.8}
      {...props}
    >
      {isLoading ? (
        <ActivityIndicator color={variant === 'outline' ? '#2563eb' : '#ffffff'} />
      ) : (
        <Text className={textClasses[variant]}>{title}</Text>
      )}
    </TouchableOpacity>
  );
}

