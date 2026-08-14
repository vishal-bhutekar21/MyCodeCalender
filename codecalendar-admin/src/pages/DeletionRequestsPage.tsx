import React, { useEffect, useState } from 'react';
import {
  UserX,
  CheckCircle2,
  Clock,
  Trash2,
  ShieldAlert,
  Loader2
} from 'lucide-react';
import { GlassCard } from '../components/ui/GlassCard';
import { DeleteConfirmModal } from '../components/ui/DeleteConfirmModal';
import type { DeletionRequest } from '../types';
import {
  subscribeToDeletionRequests,
  approveAndExecuteDataDeletion,
  rejectDeletionRequest
} from '../services/firestoreService';

export const DeletionRequestsPage: React.FC = () => {
  const [requests, setRequests] = useState<DeletionRequest[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Approval modal state
  const [selectedReq, setSelectedReq] = useState<DeletionRequest | null>(null);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);

  useEffect(() => {
    const unsubscribe = subscribeToDeletionRequests((items) => {
      setRequests(items);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleApprove = async () => {
    if (!selectedReq) return;
    setIsProcessing(true);
    try {
      await approveAndExecuteDataDeletion(selectedReq.id, selectedReq.uid);
      setSelectedReq(null);
    } catch (err) {
      console.error('Failed to approve deletion:', err);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleReject = async (id: string) => {
    try {
      await rejectDeletionRequest(id);
    } catch (err) {
      console.error('Failed to reject deletion request:', err);
    }
  };

  const pendingRequests = requests.filter((r) => r.status === 'PENDING');
  const resolvedRequests = requests.filter((r) => r.status !== 'PENDING');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <UserX className="w-6 h-6 text-rose-500" />
            <span>Account & Data Deletion Queue</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Google Play Data Safety & GDPR compliance dashboard to review user erasure tickets.
          </p>
        </div>

        <div className="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-xs font-semibold text-rose-400">
          <ShieldAlert className="w-4 h-4" />
          <span>Pending Tickets: {pendingRequests.length}</span>
        </div>
      </div>

      {/* Pending Tickets Section */}
      <div className="space-y-3">
        <h2 className="text-sm font-bold uppercase tracking-wider text-slate-300">
          Pending Action Required ({pendingRequests.length})
        </h2>

        {loading ? (
          <div className="py-12 flex flex-col items-center justify-center text-slate-400">
            <Loader2 className="w-6 h-6 animate-spin text-brand-orange mb-2" />
            <p className="text-xs">Checking queue...</p>
          </div>
        ) : pendingRequests.length === 0 ? (
          <GlassCard className="p-8 text-center">
            <div className="p-3 rounded-full bg-emerald-500/10 text-emerald-400 w-12 h-12 mx-auto flex items-center justify-center mb-2">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-white">Queue is clear</h3>
            <p className="text-xs text-slate-400 mt-1">
              No outstanding user deletion requests at this time.
            </p>
          </GlassCard>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {pendingRequests.map((req) => (
              <GlassCard key={req.id} className="p-5 border-rose-500/30 bg-rose-950/10 space-y-4">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-base font-bold text-white">{req.displayName}</h3>
                    <p className="text-xs font-mono text-slate-300">{req.email}</p>
                    <p className="text-[10px] text-slate-400 font-mono mt-0.5">UID: {req.uid}</p>
                  </div>
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30 flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    <span>PENDING</span>
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-black/40 border border-white/5 text-xs text-slate-300">
                  <p className="font-semibold text-slate-400 text-[10px] uppercase mb-1">Reason Provided:</p>
                  <p className="italic">"{req.reason || 'User requested account and data deletion from App Settings'}"</p>
                </div>

                <div className="pt-2 flex items-center justify-end gap-2 border-t border-white/10">
                  <button
                    type="button"
                    onClick={() => handleReject(req.id)}
                    className="px-3 py-1.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-white hover:bg-white/10"
                  >
                    Reject Ticket
                  </button>
                  <button
                    type="button"
                    onClick={() => setSelectedReq(req)}
                    className="px-4 py-1.5 rounded-xl text-xs font-bold bg-rose-600 hover:bg-rose-500 text-white shadow-md shadow-rose-600/30 flex items-center gap-1.5"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    <span>Erase User Data</span>
                  </button>
                </div>
              </GlassCard>
            ))}
          </div>
        )}
      </div>

      {/* Resolved Tickets Audit Log */}
      {resolvedRequests.length > 0 && (
        <div className="space-y-3 pt-6">
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-400">
            Resolved Audit History ({resolvedRequests.length})
          </h2>

          <GlassCard className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="border-b border-white/10 bg-black/30 text-slate-400 font-semibold uppercase">
                  <tr>
                    <th className="py-3 px-5">User</th>
                    <th className="py-3 px-5">Status</th>
                    <th className="py-3 px-5">Reason</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  {resolvedRequests.map((req) => (
                    <tr key={req.id}>
                      <td className="py-3.5 px-5">
                        <p className="font-bold text-white">{req.displayName}</p>
                        <p className="text-[11px] text-slate-400 font-mono">{req.email}</p>
                      </td>
                      <td className="py-3.5 px-5">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          req.status === 'COMPLETED'
                            ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                            : 'bg-slate-500/20 text-slate-300'
                        }`}>
                          {req.status}
                        </span>
                      </td>
                      <td className="py-3.5 px-5 text-slate-300 text-[11px] max-w-sm truncate">
                        {req.reason}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </GlassCard>
        </div>
      )}

      {/* Delete Execution Confirmation */}
      <DeleteConfirmModal
        isOpen={Boolean(selectedReq)}
        onClose={() => setSelectedReq(null)}
        onConfirm={handleApprove}
        title="Execute Permanent User Data Erasure"
        message={`This will permanently purge user ${selectedReq?.displayName} (${selectedReq?.email}) from the Firestore database and mark the deletion ticket as COMPLETED. This action cannot be undone.`}
        confirmButtonText="Execute Data Erasure"
        isLoading={isProcessing}
      />
    </div>
  );
};
