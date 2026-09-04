import React from 'react';
import { GlassBubble } from '@/components/foundation';

export type ConversationRole = 'user' | 'assistant';

export function ConversationBubble({ role, text }: { role: ConversationRole; text: string }) {
  return <GlassBubble text={text} align={role === 'user' ? 'right' : 'left'} />;
}

export function MessageBubbleUser({ text }: { text: string }) {
  return <ConversationBubble role="user" text={text} />;
}

export function MessageBubbleAssistant({ text }: { text: string }) {
  return <ConversationBubble role="assistant" text={text} />;
}
