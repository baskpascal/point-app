import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { colors, gradients, spacing, typography } from '@/theme';

export type TimelineStepStatus = 'completed' | 'active' | 'pending';

export type TimelineStepData = {
  id: string;
  label: string;
};

type Props = {
  steps: TimelineStepData[];
  activeIndex: number;
};

export function TimelineList({ steps, activeIndex }: Props) {
  return (
    <View>
      {steps.map((step, i) => {
        const status: TimelineStepStatus =
          i < activeIndex ? 'completed' : i === activeIndex ? 'active' : 'pending';
        const isLast = i === steps.length - 1;
        return (
          <View key={step.id} style={styles.row}>
            <View style={styles.railColumn}>
              <TimelineNode index={i + 1} status={status} />
              {!isLast && <TimelineConnector filled={status === 'completed'} />}
            </View>
            <View style={styles.labelWrap}>
              <Text
                style={[
                  styles.label,
                  status === 'pending' && styles.labelPending,
                  status === 'active' && styles.labelActive,
                ]}
              >
                {step.label}
              </Text>
            </View>
          </View>
        );
      })}
    </View>
  );
}

function TimelineNode({ index, status }: { index: number; status: TimelineStepStatus }) {
  if (status === 'active') {
    return (
      <LinearGradient colors={gradients.cta} style={[styles.node, styles.nodeActive]}>
        <Text style={styles.nodeActiveText}>{index}</Text>
      </LinearGradient>
    );
  }
  return (
    <View style={[styles.node, status === 'completed' ? styles.nodeCompleted : styles.nodePending]}>
      <Text style={status === 'completed' ? styles.nodeCompletedText : styles.nodePendingText}>
        {status === 'completed' ? '✓' : index}
      </Text>
    </View>
  );
}

function TimelineConnector({ filled }: { filled: boolean }) {
  return <View style={[styles.connector, filled && styles.connectorFilled]} />;
}

const NODE_SIZE = 34;

const styles = StyleSheet.create({
  row: { flexDirection: 'row' },
  railColumn: { alignItems: 'center', width: NODE_SIZE },
  labelWrap: { flex: 1, paddingLeft: spacing.lg, paddingBottom: spacing.xxl, paddingTop: 4 },
  node: {
    width: NODE_SIZE,
    height: NODE_SIZE,
    borderRadius: NODE_SIZE / 2,
    alignItems: 'center',
    justifyContent: 'center',
  },
  nodeActive: {
    shadowColor: colors.accentCoral,
    shadowOpacity: 0.45,
    shadowRadius: 12,
    shadowOffset: { width: 0, height: 4 },
  },
  nodeActiveText: { color: colors.white, fontWeight: '700' },
  nodeCompleted: { backgroundColor: colors.textPrimary },
  nodeCompletedText: { color: colors.white, fontWeight: '700' },
  nodePending: { backgroundColor: 'rgba(16,24,40,0.06)' },
  nodePendingText: { color: colors.textTertiary, fontWeight: '600' },
  connector: { width: 2, flex: 1, minHeight: 24, backgroundColor: 'rgba(16,24,40,0.08)' },
  connectorFilled: { backgroundColor: colors.textPrimary },
  label: { ...typography.body, color: colors.textPrimary },
  labelActive: { ...typography.bodyMedium, color: colors.textPrimary },
  labelPending: { color: colors.textTertiary },
});
