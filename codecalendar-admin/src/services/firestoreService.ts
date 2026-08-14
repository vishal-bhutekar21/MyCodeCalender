import {
  collection,
  doc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  query,
  where,
  orderBy,
  onSnapshot,
  serverTimestamp,
  writeBatch
} from 'firebase/firestore';
import { firestore } from './firebase';
import type {
  FeaturedMaterial,
  Broadcast,
  CustomContest,
  UserAccount,
  DeletionRequest,
  DashboardMetrics
} from '../types';

// ── FEATURED MATERIALS / ARTICLES CMS ──────────────────────────────────────────

export const subscribeToFeaturedMaterials = (
  callback: (materials: FeaturedMaterial[]) => void
) => {
  const q = query(
    collection(firestore, 'featured_materials'),
    orderBy('priority', 'asc')
  );

  return onSnapshot(q, (snapshot) => {
    const items: FeaturedMaterial[] = snapshot.docs.map((docSnap) => ({
      id: docSnap.id,
      ...(docSnap.data() as Omit<FeaturedMaterial, 'id'>)
    }));
    callback(items);
  }, (err) => {
    console.warn('Snapshot error in featured_materials:', err);
    // Fallback without orderBy if index is building
    getDocs(collection(firestore, 'featured_materials')).then((snap) => {
      const items = snap.docs.map((d) => ({ id: d.id, ...d.data() } as FeaturedMaterial));
      callback(items);
    }).catch(() => callback([]));
  });
};

export const saveFeaturedMaterial = async (
  material: Omit<FeaturedMaterial, 'id'>,
  id?: string
): Promise<string> => {
  const materialRef = id
    ? doc(firestore, 'featured_materials', id)
    : doc(collection(firestore, 'featured_materials'));

  const data = {
    ...material,
    updatedAt: serverTimestamp(),
    createdAt: id ? material.createdAt || serverTimestamp() : serverTimestamp()
  };

  await setDoc(materialRef, data, { merge: true });
  return materialRef.id;
};

export const deleteFeaturedMaterial = async (id: string): Promise<void> => {
  await deleteDoc(doc(firestore, 'featured_materials', id));
};

export const toggleMaterialActive = async (id: string, currentStatus: boolean): Promise<void> => {
  await updateDoc(doc(firestore, 'featured_materials', id), {
    isActive: !currentStatus,
    updatedAt: serverTimestamp()
  });
};

// ── IN-APP BROADCASTS CMS ───────────────────────────────────────────────────

export const subscribeToBroadcasts = (
  callback: (broadcasts: Broadcast[]) => void
) => {
  const q = query(
    collection(firestore, 'broadcasts'),
    orderBy('createdAt', 'desc')
  );

  return onSnapshot(q, (snapshot) => {
    const items: Broadcast[] = snapshot.docs.map((docSnap) => ({
      id: docSnap.id,
      ...(docSnap.data() as Omit<Broadcast, 'id'>)
    }));
    callback(items);
  }, () => {
    getDocs(collection(firestore, 'broadcasts')).then((snap) => {
      const items = snap.docs.map((d) => ({ id: d.id, ...d.data() } as Broadcast));
      callback(items);
    }).catch(() => callback([]));
  });
};

export const saveBroadcast = async (
  broadcast: Omit<Broadcast, 'id'>,
  id?: string
): Promise<string> => {
  const broadcastRef = id
    ? doc(firestore, 'broadcasts', id)
    : doc(collection(firestore, 'broadcasts'));

  const data = {
    ...broadcast,
    updatedAt: serverTimestamp(),
    createdAt: id ? broadcast.createdAt || serverTimestamp() : serverTimestamp()
  };

  await setDoc(broadcastRef, data, { merge: true });
  return broadcastRef.id;
};

export const deleteBroadcast = async (id: string): Promise<void> => {
  await deleteDoc(doc(firestore, 'broadcasts', id));
};

export const toggleBroadcastActive = async (id: string, currentStatus: boolean): Promise<void> => {
  await updateDoc(doc(firestore, 'broadcasts', id), {
    isActive: !currentStatus,
    updatedAt: serverTimestamp()
  });
};

// ── CUSTOM CONTESTS & HACKATHONS CMS ─────────────────────────────────────────

export const subscribeToCustomContests = (
  callback: (contests: CustomContest[]) => void
) => {
  const q = query(
    collection(firestore, 'custom_contests'),
    orderBy('startTime', 'asc')
  );

  return onSnapshot(q, (snapshot) => {
    const items: CustomContest[] = snapshot.docs.map((docSnap) => ({
      id: docSnap.id,
      ...(docSnap.data() as Omit<CustomContest, 'id'>)
    }));
    callback(items);
  }, () => {
    getDocs(collection(firestore, 'custom_contests')).then((snap) => {
      const items = snap.docs.map((d) => ({ id: d.id, ...d.data() } as CustomContest));
      callback(items);
    }).catch(() => callback([]));
  });
};

export const saveCustomContest = async (
  contest: Omit<CustomContest, 'id'>,
  id?: string
): Promise<string> => {
  const contestRef = id
    ? doc(firestore, 'custom_contests', id)
    : doc(collection(firestore, 'custom_contests'));

  const data = {
    ...contest,
    createdAt: id ? contest.createdAt || serverTimestamp() : serverTimestamp()
  };

  await setDoc(contestRef, data, { merge: true });
  return contestRef.id;
};

export const deleteCustomContest = async (id: string): Promise<void> => {
  await deleteDoc(doc(firestore, 'custom_contests', id));
};

// ── USERS DIRECTORY ──────────────────────────────────────────────────────────

export const subscribeToUsers = (
  callback: (users: UserAccount[]) => void
) => {
  const q = collection(firestore, 'users');
  return onSnapshot(q, (snapshot) => {
    const users: UserAccount[] = snapshot.docs.map((docSnap) => ({
      uid: docSnap.id,
      ...(docSnap.data() as Omit<UserAccount, 'uid'>)
    }));
    callback(users);
  }, () => callback([]));
};

// ── DELETION REQUESTS & GDPR SAFETY ──────────────────────────────────────────

export const subscribeToDeletionRequests = (
  callback: (requests: DeletionRequest[]) => void
) => {
  const q = collection(firestore, 'deletion_requests');
  return onSnapshot(q, (snapshot) => {
    const requests: DeletionRequest[] = snapshot.docs.map((docSnap) => ({
      id: docSnap.id,
      ...(docSnap.data() as Omit<DeletionRequest, 'id'>)
    }));
    callback(requests);
  }, () => callback([]));
};

export const approveAndExecuteDataDeletion = async (
  requestId: string,
  userUid: string
): Promise<void> => {
  const batch = writeBatch(firestore);

  // 1. Delete user document from users collection
  const userDocRef = doc(firestore, 'users', userUid);
  batch.delete(userDocRef);

  // 2. Mark ticket as COMPLETED
  const reqDocRef = doc(firestore, 'deletion_requests', requestId);
  batch.update(reqDocRef, {
    status: 'COMPLETED',
    resolvedAt: serverTimestamp()
  });

  await batch.commit();
};

export const rejectDeletionRequest = async (requestId: string): Promise<void> => {
  await updateDoc(doc(firestore, 'deletion_requests', requestId), {
    status: 'REJECTED',
    resolvedAt: serverTimestamp()
  });
};

// ── DASHBOARD AGGREGATES ─────────────────────────────────────────────────────

export const fetchDashboardMetrics = async (): Promise<DashboardMetrics> => {
  try {
    const [usersSnap, broadcastsSnap, materialsSnap, deletionsSnap, contestsSnap] = await Promise.all([
      getDocs(collection(firestore, 'users')),
      getDocs(query(collection(firestore, 'broadcasts'), where('isActive', '==', true))),
      getDocs(collection(firestore, 'featured_materials')),
      getDocs(query(collection(firestore, 'deletion_requests'), where('status', '==', 'PENDING'))),
      getDocs(collection(firestore, 'custom_contests'))
    ]);

    return {
      totalUsers: usersSnap.size,
      activeBroadcasts: broadcastsSnap.size,
      publishedMaterials: materialsSnap.size,
      pendingDeletions: deletionsSnap.size,
      totalCustomContests: contestsSnap.size
    };
  } catch (err) {
    console.error('Error fetching dashboard metrics:', err);
    return {
      totalUsers: 0,
      activeBroadcasts: 0,
      publishedMaterials: 0,
      pendingDeletions: 0,
      totalCustomContests: 0
    };
  }
};
