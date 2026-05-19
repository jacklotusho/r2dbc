import { View, ViewProps } from 'react-native';

interface CardProps extends ViewProps {
  children: React.ReactNode;
  className?: string;
}

export function Card({ children, className = '', ...props }: CardProps) {
  return (
    <View
      className={`bg-white rounded-2xl shadow-2xl p-6 ${className}`}
      {...props}
    >
      {children}
    </View>
  );
}

