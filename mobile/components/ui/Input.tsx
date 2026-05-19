import { TextInput, Text, View, TextInputProps } from 'react-native';

interface InputProps extends TextInputProps {
  label?: string;
  error?: string;
}

export function Input({ label, error, ...props }: InputProps) {
  return (
    <View className="mb-5">
      {label && (
        <Text className="text-gray-700 font-semibold mb-2 text-base">
          {label}
        </Text>
      )}
      <TextInput
        className={`border-2 rounded-xl px-4 py-4 text-base bg-white ${
          error ? 'border-red-500' : 'border-gray-200 focus:border-blue-500'
        }`}
        placeholderTextColor="#9ca3af"
        {...props}
      />
      {error && (
        <Text className="text-red-500 text-sm mt-2 font-medium">
          {error}
        </Text>
      )}
    </View>
  );
}

