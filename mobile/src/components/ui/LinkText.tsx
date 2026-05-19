import { TouchableOpacity, Text, StyleSheet, TextStyle } from 'react-native';
import { Colors, Typography } from '@/src/theme';

interface LinkTextProps {
  children: string;
  onPress: () => void;
  style?: TextStyle;
}

export function LinkText({ children, onPress, style }: LinkTextProps) {
  return (
    <TouchableOpacity onPress={onPress}>
      <Text style={[styles.text, style]}>{children}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  text: {
    ...Typography.link,
    color: Colors.textLink,
  },
});


