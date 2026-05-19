import { View, Text, StyleSheet } from 'react-native';
import { Colors, Spacing } from '@/src/theme';

interface DividerProps {
  text?: string;
}

export function Divider({ text = 'or continue with' }: DividerProps) {
  return (
    <View style={styles.container}>
      <View style={styles.line} />
      <Text style={styles.text}>{text}</Text>
      <View style={styles.line} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: Spacing.xl,
  },
  line: {
    flex: 1,
    height: 1,
    backgroundColor: Colors.border,
  },
  text: {
    fontSize: 11,
    color: Colors.textMuted,
    marginHorizontal: Spacing.md,
  },
});


