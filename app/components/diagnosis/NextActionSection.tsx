import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, spacing, typography } from '@/theme';

type Props = { title?: string; items: string[] };

export function NextActionSection({ title = 'What to do next', items }: Props) {
  return (
    <View style={styles.wrap}>
      <Text style={styles.title}>{title}</Text>
      {items.map((item, i) => (
        <View key={i} style={styles.row}>
          <View style={styles.dot} />
          <Text style={styles.item}>{item}</Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { width: '100%', gap: spacing.sm },
  title: { ...typography.bodyMedium, color: colors.textPrimary, marginBottom: spacing.xs },
  row: { flexDirection: 'row', alignItems: 'flex-start', gap: spacing.sm },
  dot: { width: 6, height: 6, borderRadius: 3, backgroundColor: colors.accentOrange, marginTop: 8 },
  item: { ...typography.body, color: colors.textSecondary, flex: 1 },
});
