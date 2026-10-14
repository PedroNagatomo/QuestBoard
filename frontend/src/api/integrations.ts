import { api } from './client';

export const integrationsApi = {
  test: (payload: {
    source: 'GITHUB'|'JIRA'|'TRELLO';
    type: 'ISSUE_CREATED'|'ISSUE_COMPLETED'|'ISSUE_UPDATED'|'PR_MERGED';
    externalId: string; title: string; description?: string;
    difficultyHint?: string; url?: string;
  }) => api.post('/integrations/test', payload).then((r) => r.data),
};