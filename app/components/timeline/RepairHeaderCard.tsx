import React from 'react';
import { Image, StyleSheet, Text, View } from 'react-native';
import { GlassCard } from '@/components/foundation';
import { colors, spacing, typography } from '@/theme';

type Props = { title: string; subtitle: string; thumbnailUri?: string };

export function RepairHeaderCard({ title, subtitle, thumbnailUri }: Props) {
  return (
    <GlassCard strong style={styles.card}>
      <View style={styles.row}>
        <View style={styles.thumb}>
          {thumbnailUri ? (
            <Image source={{ uri: thumbnailUri }} style={styles.thumbImg} />
          ) : (
            <View style={styles.thumbPlaceholder} />
          )}
        </View>
        <View style={{ flex: 1 }}>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.subtitle}>{subtitle}</Text>
        </View>
      </View>
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  card: { padding: 0 },
  row: { flexDirection: 'row', alignItems: 'center', gap: spacing.lg, padding: spacing.lg },
  thumb: { width: 56, height: 56, borderRadius: 18, overflow: 'hidden' },
  thumbImg: { width: '100%', height: '100%' },
  thumbPlaceholder: { flex: 1, backgroundColor: colors.accentPeach },
  title: { ...typography.bodyMedium, color: colors.textPrimary },
  subtitle: { ...typography.caption, color: colors.textSecondary, marginTop: 2 },
});
