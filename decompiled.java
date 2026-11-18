/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  android.app.Notification
 *  android.app.NotificationChannel
 *  android.app.NotificationManager
 *  android.app.PendingIntent
 *  android.content.Context
 *  android.content.Intent
 *  android.net.Uri
 *  android.os.Build$VERSION
 *  androidx.compose.runtime.internal.StabilityInferred
 *  androidx.core.app.NotificationCompat$Builder
 *  androidx.core.content.ContextCompat
 *  androidx.lifecycle.ViewModel
 *  androidx.lifecycle.ViewModelKt
 *  com.google.android.gms.tasks.Task
 *  com.google.firebase.FirebaseApp
 *  com.google.firebase.auth.FirebaseAuth
 *  com.google.firebase.auth.FirebaseUser
 *  com.google.firebase.firestore.DocumentReference
 *  com.google.firebase.firestore.DocumentSnapshot
 *  com.google.firebase.firestore.FirebaseFirestore
 *  com.google.firebase.firestore.FirebaseFirestoreException
 *  com.google.firebase.firestore.QuerySnapshot
 *  com.google.firebase.storage.FirebaseStorage
 *  com.google.firebase.storage.StorageReference
 *  kotlin.KotlinNothingValueException
 *  kotlin.Metadata
 *  kotlin.Pair
 *  kotlin.ResultKt
 *  kotlin.TuplesKt
 *  kotlin.Unit
 *  kotlin.collections.CollectionsKt
 *  kotlin.collections.IntIterator
 *  kotlin.collections.MapsKt
 *  kotlin.comparisons.ComparisonsKt
 *  kotlin.coroutines.Continuation
 *  kotlin.coroutines.intrinsics.IntrinsicsKt
 *  kotlin.coroutines.jvm.internal.Boxing
 *  kotlin.coroutines.jvm.internal.ContinuationImpl
 *  kotlin.coroutines.jvm.internal.SpillingKt
 *  kotlin.jvm.functions.Function1
 *  kotlin.jvm.functions.Function2
 *  kotlin.jvm.functions.Function3
 *  kotlin.jvm.functions.Function4
 *  kotlin.jvm.functions.Function6
 *  kotlin.jvm.internal.Intrinsics
 *  kotlin.jvm.internal.SourceDebugExtension
 *  kotlin.ranges.RangesKt
 *  kotlin.text.StringsKt
 *  kotlinx.coroutines.BuildersKt
 *  kotlinx.coroutines.CoroutineScope
 *  kotlinx.coroutines.Job
 *  kotlinx.coroutines.flow.Flow
 *  kotlinx.coroutines.flow.FlowCollector
 *  kotlinx.coroutines.flow.FlowKt
 *  kotlinx.coroutines.flow.MutableStateFlow
 *  kotlinx.coroutines.flow.SharingStarted
 *  kotlinx.coroutines.flow.SharingStarted$Companion
 *  kotlinx.coroutines.flow.StateFlow
 *  kotlinx.coroutines.flow.StateFlowKt
 *  kotlinx.coroutines.tasks.TasksKt
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.example.viewmodel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.compose.runtime.internal.StabilityInferred;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import com.example.MainActivity;
import com.example.data.AdminDashboardMetrics;
import com.example.data.AdminLog;
import com.example.data.AnalyticsMetrics;
import com.example.data.Chapter;
import com.example.data.CoinTransaction;
import com.example.data.DayActivity;
import com.example.data.ExamCountdown;
import com.example.data.FocusSettings;
import com.example.data.GlobalAppConfig;
import com.example.data.NotificationBroadcast;
import com.example.data.ReferralRecord;
import com.example.data.RevisionRepository;
import com.example.data.RevisionSettings;
import com.example.data.StudyLog;
import com.example.data.Subject;
import com.example.data.SubjectBreakdown;
import com.example.data.SubjectPerformance;
import com.example.data.SubjectProgress;
import com.example.data.SubjectTest;
import com.example.data.Topic;
import com.example.data.TopicWithSubjectAndChapter;
import com.example.data.UserProfile;
import com.example.data.WithdrawalRequest;
import com.example.services.FocusTimerService;
import com.example.viewmodel.DashboardMetrics;
import com.example.viewmodel.RevisionViewModel;
import com.example.viewmodel.RevisionViewModel$special$;
import com.example.viewmodel.RevisionViewModelKt;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import kotlin.KotlinNothingValueException;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.collections.IntIterator;
import kotlin.collections.MapsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function6;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import kotlinx.coroutines.tasks.TasksKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(mv={2, 2, 0}, k=1, xi=48, d1={"\u0000\u00b2\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0000\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u001e\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u001a\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b)\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u0014\u0010\t\u001a\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0013J\u000e\u0010!\u001a\u00020\n2\u0006\u0010\"\u001a\u00020\u001fJ\u001a\u0010&\u001a\u00020\n2\u0012\u0010'\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020*0(J\u000e\u0010+\u001a\u00020\n2\u0006\u0010,\u001a\u00020)J\u000e\u0010-\u001a\u00020\n2\u0006\u0010,\u001a\u00020)J\u001e\u0010.\u001a\u00020\n2\u0006\u0010/\u001a\u00020)2\u0006\u00100\u001a\u00020)2\u0006\u0010,\u001a\u00020)J\u000e\u00107\u001a\u00020\n2\u0006\u00108\u001a\u000202J6\u00109\u001a\u00020\n2\u0006\u0010:\u001a\u00020)2\u0006\u0010;\u001a\u00020)2\u0006\u0010<\u001a\u00020=2\u0006\u0010>\u001a\u00020?2\u0006\u0010@\u001a\u00020?2\u0006\u0010A\u001a\u00020)J\u000e\u0010B\u001a\u00020\n2\u0006\u0010C\u001a\u000205J\u000e\u0010D\u001a\u00020\n2\u0006\u0010E\u001a\u00020)J\u0010\u0010Z\u001a\u00020\u00162\b\b\u0002\u0010[\u001a\u00020)J\u0006\u0010\\\u001a\u00020\u0016J\u0006\u0010]\u001a\u00020\u0016J\u000e\u0010j\u001a\u00020\u00162\u0006\u0010k\u001a\u00020)J\u000e\u0010o\u001a\u00020\u00162\u0006\u0010p\u001a\u00020)J;\u0010q\u001a\u00020\u00162\u0006\u0010r\u001a\u00020)2\b\u0010s\u001a\u0004\u0018\u00010=2\b\u0010t\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010u\u001a\u00020)2\b\b\u0002\u0010v\u001a\u00020)\u00a2\u0006\u0002\u0010wJ.\u0010x\u001a\u00020\u00162\u0006\u0010k\u001a\u00020)2\u0006\u0010y\u001a\u00020)2\u0006\u0010z\u001a\u00020)2\u0006\u0010{\u001a\u00020\b2\u0006\u0010|\u001a\u00020\bJ\u000e\u0010}\u001a\u00020\u00162\u0006\u0010~\u001a\u00020bJ\u000e\u0010\u007f\u001a\u00020\u00162\u0006\u0010k\u001a\u00020)JD\u0010\u0095\u0001\u001a\u00020\u00162\u0006\u0010/\u001a\u00020)2\u0006\u00100\u001a\u00020)2\u0007\u0010\u0096\u0001\u001a\u00020)2\u0006\u0010,\u001a\u00020)2\u001a\u0010\u0097\u0001\u001a\u0015\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00160\u0098\u0001J\u0010\u0010\u0099\u0001\u001a\u00020\u00162\u0007\u0010\u009a\u0001\u001a\u00020)J&\u0010\u009b\u0001\u001a\u00020\u00162\u0007\u0010\u009c\u0001\u001a\u00020)2\u0014\u0010\u0097\u0001\u001a\u000f\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00160\u009d\u0001J\u0019\u0010\u009e\u0001\u001a\u00020\n2\u0007\u0010\u009f\u0001\u001a\u00020)2\u0007\u0010\u00a0\u0001\u001a\u00020)J'\u0010\u00a1\u0001\u001a\u00020\u00162\b\u0010\u00a2\u0001\u001a\u00030\u008b\u00012\u0014\u0010\u0097\u0001\u001a\u000f\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00160\u009d\u0001J7\u0010\u00a3\u0001\u001a\u00020\u00162\u0007\u0010\u00a4\u0001\u001a\u00020)2\u0007\u0010\u00a5\u0001\u001a\u00020)2\u0006\u0010,\u001a\u00020)2\u0014\u0010\u0097\u0001\u001a\u000f\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00160\u009d\u0001J@\u0010\u00a6\u0001\u001a\u00020\u00162\u0007\u0010\u00a4\u0001\u001a\u00020)2\u0007\u0010\u00a7\u0001\u001a\u00020)2\u0007\u0010\u00a5\u0001\u001a\u00020)2\u0006\u0010,\u001a\u00020)2\u0014\u0010\u0097\u0001\u001a\u000f\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00160\u009d\u0001J\u0007\u0010\u00a8\u0001\u001a\u00020\u0016J\u0019\u0010\u00a9\u0001\u001a\u00020\u00162\u0007\u0010\u00aa\u0001\u001a\u00020\u00132\u0007\u0010\u00ab\u0001\u001a\u00020\u0013J&\u0010\u00ac\u0001\u001a\u00020\u00162\u0007\u0010\u00ad\u0001\u001a\u00020\b2\u0007\u0010\u00ae\u0001\u001a\u00020)2\u000b\b\u0002\u0010\u00af\u0001\u001a\u0004\u0018\u00010)J \u0010\u00b0\u0001\u001a\u00020\u00162\u0006\u0010[\u001a\u00020)2\u0006\u0010z\u001a\u00020)2\u0007\u0010\u00b1\u0001\u001a\u00020\bJ\u0019\u0010\u00b2\u0001\u001a\u00020\u00162\u0007\u0010\u00b3\u0001\u001a\u00020\b2\u0007\u0010\u00b1\u0001\u001a\u00020\bJ\"\u0010\u00b4\u0001\u001a\u00020\u00162\u0007\u0010\u00b5\u0001\u001a\u00020)2\u0007\u0010\u00b6\u0001\u001a\u00020)2\u0007\u0010\u00b1\u0001\u001a\u00020\bJ\"\u0010\u00c3\u0001\u001a\u00020\u00162\u0007\u0010\u00c4\u0001\u001a\u00020)2\u0007\u0010\u00ad\u0001\u001a\u00020\b2\u0007\u0010\u00ae\u0001\u001a\u00020)J\u0019\u0010\u00c5\u0001\u001a\u00020\b2\u0007\u0010\u00c4\u0001\u001a\u00020)H\u0086@\u00a2\u0006\u0003\u0010\u00c6\u0001J\u0019\u0010\u00c7\u0001\u001a\u00020\b2\u0007\u0010\u00c4\u0001\u001a\u00020)H\u0086@\u00a2\u0006\u0003\u0010\u00c6\u0001J\u001d\u0010\u00c8\u0001\u001a\u000f\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00070\u00c9\u00012\u0007\u0010\u00c4\u0001\u001a\u00020)J\u001d\u0010\u00ca\u0001\u001a\u000f\u0012\n\u0012\b\u0012\u0004\u0012\u00020G0\u00070\u00c9\u00012\u0007\u0010\u00c4\u0001\u001a\u00020)J:\u0010\u00cb\u0001\u001a\u00020\n2\u0006\u0010r\u001a\u00020)2\t\b\u0002\u0010\u00cc\u0001\u001a\u00020)2\b\b\u0002\u0010k\u001a\u00020)2\t\b\u0002\u0010\u00cd\u0001\u001a\u00020)2\t\b\u0002\u0010\u00ce\u0001\u001a\u00020\bJ2\u0010\u00cf\u0001\u001a\u00020\n2\u0006\u0010:\u001a\u00020)2\u0006\u0010r\u001a\u00020)2\u0007\u0010\u00cc\u0001\u001a\u00020)2\u0007\u0010\u00cd\u0001\u001a\u00020)2\u0007\u0010\u00ce\u0001\u001a\u00020\bJ\u0010\u0010\u00d0\u0001\u001a\u00020\n2\u0007\u0010\u00d1\u0001\u001a\u00020)J\"\u0010\u00d2\u0001\u001a\u00020\n2\u0006\u0010:\u001a\u00020)2\u0006\u0010r\u001a\u00020)2\t\b\u0002\u0010\u00d3\u0001\u001a\u00020)J\u0010\u0010\u00d4\u0001\u001a\u00020\n2\u0007\u0010\u00d1\u0001\u001a\u00020)J2\u0010\u00d5\u0001\u001a\u00020\n2\u0007\u0010\u00d6\u0001\u001a\u00020)2\u0006\u0010r\u001a\u00020)2\u0007\u0010\u00d7\u0001\u001a\u00020)2\u000f\b\u0002\u0010\u00d8\u0001\u001a\b\u0012\u0004\u0012\u00020)0\u0007JB\u0010\u00d9\u0001\u001a\u00020\n2\u0006\u0010:\u001a\u00020)2\u0006\u0010r\u001a\u00020)2\u0007\u0010\u00da\u0001\u001a\u00020)2\u0006\u0010|\u001a\u00020\b2\u0007\u0010\u00d7\u0001\u001a\u00020)2\u000f\b\u0002\u0010\u00d8\u0001\u001a\b\u0012\u0004\u0012\u00020)0\u0007JB\u0010\u00db\u0001\u001a\u00020\n2\u0006\u0010[\u001a\u00020)2\u0006\u0010r\u001a\u00020)2\u0007\u0010\u00da\u0001\u001a\u00020)2\u0006\u0010|\u001a\u00020\b2\u0007\u0010\u00d7\u0001\u001a\u00020)2\u000f\b\u0002\u0010\u00d8\u0001\u001a\b\u0012\u0004\u0012\u00020)0\u0007J\u0010\u0010\u00dc\u0001\u001a\u00020\n2\u0007\u0010\u00d1\u0001\u001a\u00020)J\u000f\u0010\u00dd\u0001\u001a\u00020\n2\u0006\u0010[\u001a\u00020)J!\u0010\u00de\u0001\u001a\u00020\n2\u0006\u0010[\u001a\u00020)2\u0007\u0010\u00df\u0001\u001a\u00020\u00132\u0007\u0010\u00e0\u0001\u001a\u00020=J\t\u0010\u00e1\u0001\u001a\u00020)H\u0002J\t\u0010\u00e2\u0001\u001a\u00020=H\u0002J\u0012\u0010\u00e3\u0001\u001a\u00020)2\u0007\u0010\u00e4\u0001\u001a\u00020=H\u0002J\u001b\u0010\u00e5\u0001\u001a\u00020\b2\u0007\u0010\u00e6\u0001\u001a\u00020\b2\u0007\u0010\u00e7\u0001\u001a\u00020\u0013H\u0002J\"\u0010\u00e8\u0001\u001a\u00020\n2\u0007\u0010\u00c4\u0001\u001a\u00020)2\u0007\u0010\u00e9\u0001\u001a\u00020\u00132\u0007\u0010\u00a7\u0001\u001a\u00020)J\u0010\u0010\u00ea\u0001\u001a\u00020\n2\u0007\u0010\u00c4\u0001\u001a\u00020)J\u0010\u0010\u00eb\u0001\u001a\u00020\n2\u0007\u0010\u00ec\u0001\u001a\u00020\u001cJ\u0010\u0010\u00ed\u0001\u001a\u00020\n2\u0007\u0010\u00ee\u0001\u001a\u00020GJ\u001e\u0010\u00ef\u0001\u001a\u00020\n2\u0006\u0010[\u001a\u00020)2\r\u0010\u00f0\u0001\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u0011\u0010\u00f1\u0001\u001a\u00020\u00162\b\u0010\u00f2\u0001\u001a\u00030\u00f3\u0001J5\u0010\u00f4\u0001\u001a\u00020\u00162\b\u0010\u00f2\u0001\u001a\u00030\u00f3\u00012\u0006\u0010[\u001a\u00020)2\u0006\u0010/\u001a\u00020)2\u0007\u0010\u00f5\u0001\u001a\u00020)2\u0007\u0010\u00f6\u0001\u001a\u00020)H\u0002JC\u0010\u00f7\u0001\u001a\u00020\n2\b\u0010\u00f8\u0001\u001a\u00030\u00f9\u00012\u0014\u0010\u00fa\u0001\u001a\u000f\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00160\u009d\u00012\u001a\u0010\u00fb\u0001\u001a\u0015\u0012\n\u0012\b0\u00fc\u0001j\u0003`\u00fd\u0001\u0012\u0004\u0012\u00020\u00160\u009d\u0001JC\u0010\u00fe\u0001\u001a\u00020\n2\b\u0010\u00ff\u0001\u001a\u00030\u0080\u00022\u0014\u0010\u00fa\u0001\u001a\u000f\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020\u00160\u009d\u00012\u001a\u0010\u00fb\u0001\u001a\u0015\u0012\n\u0012\b0\u00fc\u0001j\u0003`\u00fd\u0001\u0012\u0004\u0012\u00020\u00160\u009d\u0001R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\b0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\b0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00130\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u001d\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0011R\u0019\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001c0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0011R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0011R\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0011R\u001d\u00101\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010\u0011R\u001d\u00104\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002050\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010\u0011R\u001d\u0010F\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020G0\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u0010\u0011R\u001d\u0010I\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020J0\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010\u0011R\u0017\u0010L\u001a\b\u0012\u0004\u0012\u00020=0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bM\u0010\u0011R\u0017\u0010N\u001a\b\u0012\u0004\u0012\u00020O0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u0010\u0011R\u0017\u0010Q\u001a\b\u0012\u0004\u0012\u00020\u00130\r\u00a2\u0006\b\n\u0000\u001a\u0004\bQ\u0010RR\u0017\u0010S\u001a\b\u0012\u0004\u0012\u00020=0\r\u00a2\u0006\b\n\u0000\u001a\u0004\bT\u0010RR\u0017\u0010U\u001a\b\u0012\u0004\u0012\u00020)0\r\u00a2\u0006\b\n\u0000\u001a\u0004\bV\u0010RR\u0017\u0010W\u001a\b\u0012\u0004\u0012\u00020\b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\bX\u0010RR\u000e\u0010Y\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010^\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020_0\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b`\u0010\u0011R\u001a\u0010a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020b0\u00070\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020b0\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bd\u0010\u0011R\u0014\u0010e\u001a\b\u0012\u0004\u0012\u00020)0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010f\u001a\b\u0012\u0004\u0012\u00020)0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bg\u0010\u0011R\u0017\u0010h\u001a\b\u0012\u0004\u0012\u00020)0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bi\u0010\u0011R\u0014\u0010l\u001a\b\u0012\u0004\u0012\u00020)0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010m\u001a\b\u0012\u0004\u0012\u00020)0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\bn\u0010\u0011R \u0010\u0080\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0081\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u0082\u0001\u0010\u0011R \u0010\u0083\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0081\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u0084\u0001\u0010\u0011R \u0010\u0085\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0086\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u0087\u0001\u0010\u0011R \u0010\u0088\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0086\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u0089\u0001\u0010\u0011R \u0010\u008a\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u008b\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u008c\u0001\u0010\u0011R \u0010\u008d\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u008b\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u008e\u0001\u0010\u0011R \u0010\u008f\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0090\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u0091\u0001\u0010\u0011R \u0010\u0092\u0001\u001a\u000f\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030\u0093\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u0094\u0001\u0010\u0011R\u001a\u0010\u00b7\u0001\u001a\t\u0012\u0005\u0012\u00030\u00b8\u00010\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u00b9\u0001\u0010\u0011R\u001a\u0010\u00ba\u0001\u001a\t\u0012\u0005\u0012\u00030\u00bb\u00010\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u00bc\u0001\u0010\u0011R\u001d\u0010\u00bd\u0001\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\u00070\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R!\u0010\u00be\u0001\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\u00070\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u00bf\u0001\u0010\u0011R\u001a\u0010\u00c0\u0001\u001a\t\u0012\u0005\u0012\u00030\u00c1\u00010\u000f\u00a2\u0006\t\n\u0000\u001a\u0005\b\u00c2\u0001\u0010\u0011\u00a8\u0006\u0081\u0002"}, d2={"Lcom/example/viewmodel/RevisionViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/example/data/RevisionRepository;", "<init>", "(Lcom/example/data/RevisionRepository;)V", "getRevisionPattern", "", "", "updateRevisionPattern", "Lkotlinx/coroutines/Job;", "pattern", "_unreadNotificationsCount", "Lkotlinx/coroutines/flow/MutableStateFlow;", "unreadNotificationsCount", "Lkotlinx/coroutines/flow/StateFlow;", "getUnreadNotificationsCount", "()Lkotlinx/coroutines/flow/StateFlow;", "_isDarkMode", "", "isDarkMode", "setDarkMode", "", "enabled", "allSubjects", "Lcom/example/data/Subject;", "getAllSubjects", "currentUserProfile", "Lcom/example/data/UserProfile;", "getCurrentUserProfile", "revisionSettings", "Lcom/example/data/RevisionSettings;", "getRevisionSettings", "updateRevisionSettings", "settings", "globalConfig", "Lcom/example/data/GlobalAppConfig;", "getGlobalConfig", "updateGlobalConfig", "updates", "", "", "", "resetAllUserStreaks", "adminEmail", "resetAllUserCoins", "sendEmergencyBroadcast", "title", "message", "allChapters", "Lcom/example/data/Chapter;", "getAllChapters", "allSubjectTests", "Lcom/example/data/SubjectTest;", "getAllSubjectTests", "updateChapter", "chapter", "addSubjectTest", "subjectId", "testName", "dateMs", "", "marksObtained", "", "totalMarks", "remarks", "updateSubjectTest", "test", "deleteSubjectTest", "testId", "allTopics", "Lcom/example/data/Topic;", "getAllTopics", "allTopicsWithDetails", "Lcom/example/data/TopicWithSubjectAndChapter;", "getAllTopicsWithDetails", "remainingTimeMs", "getRemainingTimeMs", "timerState", "Lcom/example/services/FocusTimerService$TimerState;", "getTimerState", "isTimerRunning", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "timerElapsedMs", "getTimerElapsedMs", "activeTimerTopicId", "getActiveTimerTopicId", "activeTimerSessionCount", "getActiveTimerSessionCount", "lastAwardedMins", "startTimer", "topicId", "pauseTimer", "resetTimer", "allStudyLogs", "Lcom/example/data/StudyLog;", "getAllStudyLogs", "rawExams", "Lcom/example/data/ExamCountdown;", "allExams", "getAllExams", "_selectedExamId", "selectedExamId", "getSelectedExamId", "activeExamId", "getActiveExamId", "selectExam", "examId", "_analyticsTimeFilter", "analyticsTimeFilter", "getAnalyticsTimeFilter", "setAnalyticsTimeFilter", "filter", "insertExam", "name", "examDateMs", "targetStudyHours", "category", "targetRank", "(Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;)V", "insertExtendedSyllabus", "subjectName", "topicName", "totalChapters", "estimatedRevisionTimeMins", "updateExam", "exam", "deleteExam", "coinTransactions", "Lcom/example/data/CoinTransaction;", "getCoinTransactions", "allCoinTransactions", "getAllCoinTransactions", "referrals", "Lcom/example/data/ReferralRecord;", "getReferrals", "allReferrals", "getAllReferrals", "withdrawalRequests", "Lcom/example/data/WithdrawalRequest;", "getWithdrawalRequests", "allWithdrawalRequests", "getAllWithdrawalRequests", "adminLogs", "Lcom/example/data/AdminLog;", "getAdminLogs", "broadcastAnalytics", "Lcom/example/data/NotificationBroadcast;", "getBroadcastAnalytics", "sendBroadcastNotification", "audience", "onResult", "Lkotlin/Function2;", "markBroadcastAsOpened", "broadcastId", "submitReferralCode", "code", "Lkotlin/Function1;", "logSystemAction", "action", "details", "submitWithdrawalRequest", "req", "approveWithdrawal", "reqId", "adminId", "rejectWithdrawal", "reason", "evaluateReferralPayouts", "updateVerificationStatus", "emailVerified", "mobileVerified", "awardCoins", "amount", "description", "sessionId", "recordRevisionEarnings", "coins", "recordStreakReward", "streakDays", "recordAchievementBonus", "achievementId", "achievementName", "dashboardMetrics", "Lcom/example/viewmodel/DashboardMetrics;", "getDashboardMetrics", "analyticsMetrics", "Lcom/example/data/AnalyticsMetrics;", "getAnalyticsMetrics", "_adminAllUsers", "adminAllUsers", "getAdminAllUsers", "adminDashboardMetrics", "Lcom/example/data/AdminDashboardMetrics;", "getAdminDashboardMetrics", "adminAwardCoins", "targetUserId", "getUserSubjectsCount", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUserTopicsCount", "getAdminSubjects", "Lkotlinx/coroutines/flow/Flow;", "getAdminTopics", "addSubject", "colorHex", "icon", "totalTopicsExpected", "updateSubject", "deleteSubject", "id", "addChapter", "importance", "deleteChapter", "addTopic", "chapterId", "notes", "tags", "addTopicWithSubjectContext", "difficulty", "updateTopicDetails", "deleteTopic", "updateTopicCompletion", "recordTimerSession", "isRevision", "durationMs", "currentDayString", "getStartOfDayMs", "formatTime", "ms", "calculateMastery", "revisions", "consistency", "banUser", "isBanned", "deleteUser", "updateUserProfile", "profile", "updateTopic", "topic", "saveCustomRevisionPlan", "intervals", "checkAndTriggerNotifications", "context", "Landroid/content/Context;", "showRealLocalNotification", "msg", "type", "uploadProfileImage", "uri", "Landroid/net/Uri;", "onSuccess", "onFailure", "Ljava/lang/Exception;", "Lkotlin/Exception;", "uploadProfileImageBytes", "bytes", "", "app"})
@StabilityInferred(parameters=0)
@SourceDebugExtension(value={"SMAP\nRevisionViewModel.kt\nKotlin\n*S Kotlin\n*F\n+ 1 RevisionViewModel.kt\ncom/example/viewmodel/RevisionViewModel\n+ 2 Transform.kt\nkotlinx/coroutines/flow/FlowKt__TransformKt\n+ 3 Emitters.kt\nkotlinx/coroutines/flow/FlowKt__EmittersKt\n+ 4 SafeCollector.common.kt\nkotlinx/coroutines/flow/internal/SafeCollector_commonKt\n+ 5 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,1048:1\n49#2:1049\n51#2:1053\n46#3:1050\n51#3:1052\n105#4:1051\n1#5:1054\n*S KotlinDebug\n*F\n+ 1 RevisionViewModel.kt\ncom/example/viewmodel/RevisionViewModel\n*L\n672#1:1049\n672#1:1053\n672#1:1050\n672#1:1052\n672#1:1051\n*E\n"})
public final class RevisionViewModel
extends ViewModel {
    @NotNull
    private final RevisionRepository repository;
    @NotNull
    private final MutableStateFlow<Integer> _unreadNotificationsCount;
    @NotNull
    private final StateFlow<Integer> unreadNotificationsCount;
    @NotNull
    private final MutableStateFlow<Boolean> _isDarkMode;
    @NotNull
    private final StateFlow<Boolean> isDarkMode;
    @NotNull
    private final StateFlow<List<Subject>> allSubjects;
    @NotNull
    private final StateFlow<UserProfile> currentUserProfile;
    @NotNull
    private final StateFlow<RevisionSettings> revisionSettings;
    @NotNull
    private final StateFlow<GlobalAppConfig> globalConfig;
    @NotNull
    private final StateFlow<List<Chapter>> allChapters;
    @NotNull
    private final StateFlow<List<SubjectTest>> allSubjectTests;
    @NotNull
    private final StateFlow<List<Topic>> allTopics;
    @NotNull
    private final StateFlow<List<TopicWithSubjectAndChapter>> allTopicsWithDetails;
    @NotNull
    private final StateFlow<Long> remainingTimeMs;
    @NotNull
    private final StateFlow<FocusTimerService.TimerState> timerState;
    @NotNull
    private final MutableStateFlow<Boolean> isTimerRunning;
    @NotNull
    private final MutableStateFlow<Long> timerElapsedMs;
    @NotNull
    private final MutableStateFlow<String> activeTimerTopicId;
    @NotNull
    private final MutableStateFlow<Integer> activeTimerSessionCount;
    private int lastAwardedMins;
    @NotNull
    private final StateFlow<List<StudyLog>> allStudyLogs;
    @NotNull
    private final StateFlow<List<ExamCountdown>> rawExams;
    @NotNull
    private final StateFlow<List<ExamCountdown>> allExams;
    @NotNull
    private final MutableStateFlow<String> _selectedExamId;
    @NotNull
    private final StateFlow<String> selectedExamId;
    @NotNull
    private final StateFlow<String> activeExamId;
    @NotNull
    private final MutableStateFlow<String> _analyticsTimeFilter;
    @NotNull
    private final StateFlow<String> analyticsTimeFilter;
    @NotNull
    private final StateFlow<List<CoinTransaction>> coinTransactions;
    @NotNull
    private final StateFlow<List<CoinTransaction>> allCoinTransactions;
    @NotNull
    private final StateFlow<List<ReferralRecord>> referrals;
    @NotNull
    private final StateFlow<List<ReferralRecord>> allReferrals;
    @NotNull
    private final StateFlow<List<WithdrawalRequest>> withdrawalRequests;
    @NotNull
    private final StateFlow<List<WithdrawalRequest>> allWithdrawalRequests;
    @NotNull
    private final StateFlow<List<AdminLog>> adminLogs;
    @NotNull
    private final StateFlow<List<NotificationBroadcast>> broadcastAnalytics;
    @NotNull
    private final StateFlow<DashboardMetrics> dashboardMetrics;
    @NotNull
    private final StateFlow<AnalyticsMetrics> analyticsMetrics;
    @NotNull
    private final MutableStateFlow<List<UserProfile>> _adminAllUsers;
    @NotNull
    private final StateFlow<List<UserProfile>> adminAllUsers;
    @NotNull
    private final StateFlow<AdminDashboardMetrics> adminDashboardMetrics;
    public static final int $stable = 8;

    public RevisionViewModel(@NotNull RevisionRepository repository) {
        Intrinsics.checkNotNullParameter((Object)repository, (String)"repository");
        this.repository = repository;
        this._unreadNotificationsCount = StateFlowKt.MutableStateFlow((Object)0);
        this.unreadNotificationsCount = (StateFlow)this._unreadNotificationsCount;
        this._isDarkMode = StateFlowKt.MutableStateFlow((Object)false);
        this.isDarkMode = FlowKt.asStateFlow(this._isDarkMode);
        try {
            String uid;
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String string = uid = firebaseUser != null ? firebaseUser.getUid() : null;
            if (uid != null) {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                Intrinsics.checkNotNullExpressionValue((Object)firebaseFirestore, (String)"getInstance(...)");
                FirebaseFirestore db = firebaseFirestore;
                db.collection("users").document(uid).collection("notifications").whereEqualTo("isRead", (Object)false).addSnapshotListener((arg_0, arg_1) -> RevisionViewModel._init_$lambda$0(this, arg_0, arg_1));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.allSubjects = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllSubjects(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends Subject>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<Subject>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.currentUserProfile = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getCurrentUserProfile(), (Function3)((Function3)new Function3<FlowCollector<? super UserProfile>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super UserProfile> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), null);
        this.revisionSettings = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getRevisionSettings(), (Function3)((Function3)new Function3<FlowCollector<? super RevisionSettings>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super RevisionSettings> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)new RevisionSettings(0, 0, 0, 0, 0, 0, false, null, false, false, false, false, null, 8191, null));
        this.globalConfig = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getGlobalConfig(), (Function3)((Function3)new Function3<FlowCollector<? super GlobalAppConfig>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super GlobalAppConfig> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)new GlobalAppConfig(false, false, 0, 0L, null, null, 0L, null, false, false, false, false, 0, false, 0, 0L, 65535, null));
        this.allChapters = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllChapters(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends Chapter>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<Chapter>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allSubjectTests = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllSubjectTests(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends SubjectTest>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<SubjectTest>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allTopics = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllTopics(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends Topic>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<Topic>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allTopicsWithDetails = FlowKt.stateIn((Flow)FlowKt.catch((Flow)FlowKt.combine(this.repository.getAllTopics(), this.repository.getAllChapters(), this.repository.getAllSubjects(), (Function4)((Function4)new Function4<List<? extends Topic>, List<? extends Chapter>, List<? extends Subject>, Continuation<? super List<? extends TopicWithSubjectAndChapter>>, Object>(null){
            int label;
            /* synthetic */ Object L$0;
            /* synthetic */ Object L$1;
            /* synthetic */ Object L$2;

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var2_2 = (List)this.L$0;
                var3_3 = (List)this.L$1;
                var4_4 = (List)this.L$2;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        $this$map\1 = (Iterable)topics;
                        $i$f$map\1\157 = false;
                        var7_7 = $this$map\1;
                        destination\2 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)$this$map\1, (int)10));
                        $i$f$mapTo\2\1049 = false;
                        for (T item\2 : $this$mapTo\2) {
                            var12_12 = (Topic)item\2;
                            var23_24 = destination\2;
                            $i$a$-map-RevisionViewModel$allTopicsWithDetails$1$1\3\1051\0 = false;
                            var14_14 = (Iterable)chapters;
                            for (Object var16_16 : var14_14) {
                                it\4 = (Chapter)var16_16;
                                $i$a$-find-RevisionViewModel$allTopicsWithDetails$1$1$chapter$1\4\158\3 = false;
                                if (!Intrinsics.areEqual((Object)it\4.getId(), (Object)topic\3.getChapterId())) continue;
                                v0 = var16_16;
                                ** GOTO lbl26
                            }
                            v0 = null;
lbl26:
                            // 2 sources

                            chapter\3 = v0;
                            if (StringsKt.isBlank((CharSequence)topic\3.getSubjectId()) == false) {
                                v1 = topic\3.getSubjectId();
                            } else {
                                v2 = chapter\3;
                                v1 = v2 != null ? v2.getSubjectId() : null;
                            }
                            subjectIdResolved\3 = v1;
                            var16_16 = (Iterable)subjects;
                            var17_17 = var16_16.iterator();
                            while (var17_17.hasNext()) {
                                var18_19 = var17_17.next();
                                it\5 = (Subject)var18_19;
                                $i$a$-find-RevisionViewModel$allTopicsWithDetails$1$1$subject$1\5\160\3 = false;
                                if (!Intrinsics.areEqual((Object)it\5.getId(), (Object)subjectIdResolved\3)) continue;
                                v3 = var18_19;
                                ** GOTO lbl43
                            }
                            v3 = null;
lbl43:
                            // 2 sources

                            subject\3 = v3;
                            v4 = topic\3.getId();
                            v5 = topic\3.getName();
                            v6 = topic\3.getNotes();
                            v7 = topic\3.getDifficulty();
                            v8 = topic\3.getEstimatedRevisionTimeMins();
                            v9 = topic\3.getStatus();
                            v10 = topic\3.getStudyTimeMs();
                            v11 = topic\3.getRevisionTimeMs();
                            v12 = topic\3.getRevisionCount();
                            v13 = topic\3.getLastRevisionDateMs();
                            v14 = topic\3.getNextRevisionDateMs();
                            v15 = topic\3.getMasteryScore();
                            v16 = chapter\3;
                            if (v16 == null || (v16 = v16.getId()) == null) {
                                v16 = "";
                            }
                            if ((v17 = chapter\3) == null || (v17 = v17.getName()) == null) {
                                v17 = "Unknown Chapter";
                            }
                            if ((v18 = subjectIdResolved\3) == null) {
                                v18 = "";
                            }
                            if ((v19 = subject\3) == null || (v19 = v19.getName()) == null) {
                                v19 = "Unknown Subject";
                            }
                            if ((v20 = subject\3) == null || (v20 = v20.getColorHex()) == null) {
                                v20 = "#0061A4";
                            }
                            var23_24.add(new TopicWithSubjectAndChapter(v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, (String)v16, (String)v17, v18, (String)v19, (String)v20, topic\3.getExamIds(), topic\3.getTags()));
                        }
                        return (List)destination\2;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(List<Topic> p1, List<Chapter> p2, List<Subject> p3, Continuation<? super List<TopicWithSubjectAndChapter>> p4) {
                var var5_5 = new /* invalid duplicate definition of identical inner class */;
                var5_5.L$0 = p1;
                var5_5.L$1 = p2;
                var5_5.L$2 = p3;
                return var5_5.invokeSuspend(Unit.INSTANCE);
            }
        })), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends TopicWithSubjectAndChapter>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<TopicWithSubjectAndChapter>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.remainingTimeMs = FocusTimerService.Companion.getRemainingTimeMs();
        this.timerState = FocusTimerService.Companion.getTimerState();
        this.isTimerRunning = StateFlowKt.MutableStateFlow((Object)false);
        this.timerElapsedMs = StateFlowKt.MutableStateFlow((Object)0L);
        this.activeTimerTopicId = StateFlowKt.MutableStateFlow((Object)"");
        this.activeTimerSessionCount = StateFlowKt.MutableStateFlow((Object)1);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = this.this$0.getTimerState().collect(new FlowCollector(){

                            public final Object emit(FocusTimerService.TimerState state, Continuation<? super Unit> $completion) {
                                this$0.isTimerRunning().setValue((Object)Boxing.boxBoolean((state == FocusTimerService.TimerState.RUNNING ? 1 : 0) != 0));
                                if (state == FocusTimerService.TimerState.COMPLETED) {
                                    Object object = (UserProfile)this$0.getCurrentUserProfile().getValue();
                                    int durationMin = object != null && (object = ((UserProfile)object).getFocusSettings()) != null ? ((FocusSettings)object).getStudyModeDurationMins() : 45;
                                    this$0.getTimerElapsedMs().setValue((Object)Boxing.boxLong((long)((long)(durationMin * 60) * 1000L)));
                                }
                                return Unit.INSTANCE;
                            }
                        }, (Continuation)this);
                        if (object2 != object) throw new KotlinNothingValueException();
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        throw new KotlinNothingValueException();
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = this.this$0.getRemainingTimeMs().collect(new FlowCollector(){

                            public final Object emit(long remaining, Continuation<? super Unit> $completion) {
                                Object object = (UserProfile)this$0.getCurrentUserProfile().getValue();
                                long durationMin = object != null && (object = ((UserProfile)object).getFocusSettings()) != null ? (long)((FocusSettings)object).getStudyModeDurationMins() : 45L;
                                long totalDuration = durationMin * (long)60 * 1000L;
                                if (remaining > 0L) {
                                    this$0.getTimerElapsedMs().setValue((Object)Boxing.boxLong((long)(totalDuration - remaining)));
                                    int currentMins = (int)(((Number)this$0.getTimerElapsedMs().getValue()).longValue() / (long)60000);
                                    if (currentMins > this$0.lastAwardedMins) {
                                        int newCoins = currentMins - this$0.lastAwardedMins;
                                        this$0.lastAwardedMins = currentMins;
                                        this$0.awardCoins(newCoins, "Study Timer Reward (minute by minute)", "timer_" + UUID.randomUUID());
                                    }
                                }
                                return Unit.INSTANCE;
                            }
                        }, (Continuation)this);
                        if (object2 != object) throw new KotlinNothingValueException();
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        throw new KotlinNothingValueException();
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
        this.allStudyLogs = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllStudyLogs(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends StudyLog>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<StudyLog>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.rawExams = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllExams(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends ExamCountdown>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<ExamCountdown>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allExams = FlowKt.stateIn((Flow)FlowKt.catch((Flow)FlowKt.combine((Flow)((Flow)this.rawExams), (Flow)((Flow)this.allTopics), (Function3)((Function3)new Function3<List<? extends ExamCountdown>, List<? extends Topic>, Continuation<? super List<? extends ExamCountdown>>, Object>(null){
            int label;
            /* synthetic */ Object L$0;
            /* synthetic */ Object L$1;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                List list = (List)this.L$0;
                List list2 = (List)this.L$1;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void $this$mapTo\2;
                        void exams;
                        ResultKt.throwOnFailure((Object)$result);
                        Iterable iterable = (Iterable)exams;
                        boolean bl = false;
                        Iterable iterable2 = iterable;
                        Collection collection = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)iterable, (int)10));
                        boolean bl2 = false;
                        for (T t : $this$mapTo\2) {
                            int n;
                            void exam\3;
                            void $this$filterTo\5;
                            void topics;
                            ExamCountdown examCountdown = (ExamCountdown)t;
                            Collection collection2 = collection;
                            boolean bl3 = false;
                            Iterable iterable3 = (Iterable)topics;
                            boolean bl4 = false;
                            Iterable iterable4 = iterable3;
                            Collection collection3 = new ArrayList<E>();
                            boolean bl5 = false;
                            for (T t2 : $this$filterTo\5) {
                                Topic topic = (Topic)t2;
                                boolean bl6 = false;
                                if (!topic.getExamIds().contains(exam\3.getId())) continue;
                                collection3.add(t2);
                            }
                            List list3 = (List)collection3;
                            Iterable iterable5 = list3;
                            boolean bl7 = false;
                            if (iterable5 instanceof Collection && ((Collection)iterable5).isEmpty()) {
                                n = 0;
                            } else {
                                int n2 = 0;
                                for (T t3 : iterable5) {
                                    Topic topic = (Topic)t3;
                                    boolean bl8 = false;
                                    if (!Intrinsics.areEqual((Object)topic.getStatus(), (Object)"Complete") || ++n2 >= 0) continue;
                                    CollectionsKt.throwCountOverflow();
                                }
                                n = n2;
                            }
                            int n3 = n;
                            int n4 = list3.size();
                            float f = n4 > 0 ? (float)n3 * 100.0f / (float)n4 : 0.0f;
                            collection2.add(ExamCountdown.copy$default((ExamCountdown)exam\3, null, null, null, null, null, 0L, f, null, null, 447, null));
                        }
                        return (List)collection;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(List<ExamCountdown> p1, List<Topic> p2, Continuation<? super List<ExamCountdown>> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p1;
                var4_4.L$1 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends ExamCountdown>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<ExamCountdown>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this._selectedExamId = StateFlowKt.MutableStateFlow((Object)"all");
        this.selectedExamId = FlowKt.asStateFlow(this._selectedExamId);
        this.activeExamId = FlowKt.stateIn((Flow)FlowKt.catch((Flow)FlowKt.combine((Flow)((Flow)this._selectedExamId), (Flow)((Flow)this.allExams), (Function3)((Function3)new Function3<String, List<? extends ExamCountdown>, Continuation<? super String>, Object>(null){
            int label;
            /* synthetic */ Object L$0;
            /* synthetic */ Object L$1;

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var2_2 = (String)this.L$0;
                var3_3 = (List)this.L$1;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        if (!exams.isEmpty()) ** GOTO lbl11
                        v0 = "";
                        ** GOTO lbl28
lbl11:
                        // 1 sources

                        if (((CharSequence)selectedId).length() == 0 || Intrinsics.areEqual((Object)selectedId, (Object)"all")) ** GOTO lbl-1000
                        $this$none\1 = (Iterable)exams;
                        $i$f$none\1\308 = false;
                        if (!($this$none\1 instanceof Collection) || !((Collection)$this$none\1).isEmpty()) ** GOTO lbl17
                        v1 = true;
                        ** GOTO lbl24
lbl17:
                        // 2 sources

                        for (T element\1 : $this$none\1) {
                            it\2 = (ExamCountdown)element\1;
                            $i$a$-none-RevisionViewModel$activeExamId$1$1\2\1050\0 = false;
                            if (!Intrinsics.areEqual((Object)it\2.getId(), (Object)selectedId)) continue;
                            v1 = false;
                            ** GOTO lbl24
                        }
                        v1 = true;
lbl24:
                        // 3 sources

                        if (v1) lbl-1000:
                        // 2 sources

                        {
                            v0 = ((ExamCountdown)CollectionsKt.first((List)exams)).getId();
                        } else {
                            v0 = selectedId;
                        }
lbl28:
                        // 3 sources

                        return v0;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(String p1, List<ExamCountdown> p2, Continuation<? super String> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p1;
                var4_4.L$1 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (Function3)((Function3)new Function3<FlowCollector<? super String>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super String> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)"");
        this._analyticsTimeFilter = StateFlowKt.MutableStateFlow((Object)"Overall");
        this.analyticsTimeFilter = FlowKt.asStateFlow(this._analyticsTimeFilter);
        this.coinTransactions = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getCoinTransactions(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends CoinTransaction>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<CoinTransaction>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allCoinTransactions = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllCoinTransactions(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends CoinTransaction>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<CoinTransaction>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.referrals = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getReferrals(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends ReferralRecord>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<ReferralRecord>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allReferrals = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllReferrals(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends ReferralRecord>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<ReferralRecord>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.withdrawalRequests = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getWithdrawalRequests(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends WithdrawalRequest>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<WithdrawalRequest>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.allWithdrawalRequests = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAllWithdrawalRequests(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends WithdrawalRequest>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<WithdrawalRequest>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.adminLogs = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getAdminLogs(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends AdminLog>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<AdminLog>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.broadcastAnalytics = FlowKt.stateIn((Flow)FlowKt.catch(this.repository.getBroadcastAnalytics(), (Function3)((Function3)new Function3<FlowCollector<? super List<? extends NotificationBroadcast>>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super List<NotificationBroadcast>> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)CollectionsKt.emptyList());
        this.dashboardMetrics = FlowKt.stateIn((Flow)FlowKt.catch((Flow)FlowKt.combine((Flow)((Flow)this.allTopicsWithDetails), (Flow)((Flow)this.activeExamId), (Flow)((Flow)this.allStudyLogs), (Function4)((Function4)new Function4<List<? extends TopicWithSubjectAndChapter>, String, List<? extends StudyLog>, Continuation<? super DashboardMetrics>, Object>(this, null){
            int label;
            /* synthetic */ Object L$0;
            /* synthetic */ Object L$1;
            /* synthetic */ Object L$2;
            final /* synthetic */ RevisionViewModel this$0;
            {
                this.this$0 = $receiver;
                super(4, $completion);
            }

            /*
             * Unable to fully structure code
             * Could not resolve type clashes
             */
            public final Object invokeSuspend(Object $result) {
                var2_2 = (List)this.L$0;
                var3_3 = (String)this.L$1;
                var4_4 = (List)this.L$2;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        if (((CharSequence)activeId).length() == 0 || Intrinsics.areEqual((Object)activeId, (Object)"all")) {
                            v0 = allTopics;
                        } else {
                            $this$filter\1 = (Iterable)allTopics;
                            $i$f$filter\1\472 = false;
                            var8_8 = $this$filter\1;
                            destination\2 = new ArrayList<E>();
                            $i$f$filterTo\2\1049 = false;
                            for (Iterator<T> element\2 : $this$filterTo\2) {
                                it\3 = (TopicWithSubjectAndChapter)element\2 /* !! */ ;
                                $i$a$-filter-RevisionViewModel$dashboardMetrics$1$topics$1\3\1050\0 = false;
                                if (!it\3.getExamIds().contains(activeId)) continue;
                                destination\2.add(element\2 /* !! */ );
                            }
                            v0 = (List)destination\2;
                        }
                        topics = v0;
                        now = System.currentTimeMillis();
                        todayStr = RevisionViewModel.access$currentDayString(this.this$0);
                        var11_12 = (Iterable)logs;
                        element\2 /* !! */  = var11_12.iterator();
                        while (element\2 /* !! */ .hasNext()) {
                            var13_15 = element\2 /* !! */ .next();
                            it\5 = (StudyLog)var13_15;
                            $i$a$-find-RevisionViewModel$dashboardMetrics$1$todayLog$1\5\477\0 = false;
                            if (!Intrinsics.areEqual((Object)it\5.getDateString(), (Object)todayStr)) continue;
                            v1 = var13_15;
                            ** GOTO lbl39
                        }
                        v1 = null;
lbl39:
                        // 2 sources

                        v2 = todayLog = (StudyLog)v1;
                        v3 = todayLog;
                        todayStudyTimeMs = (v2 != null ? v2.getStudyTimeMs() : 0L) + (v3 != null ? v3.getRevisionTimeMs() : 0L);
                        startOfTodayMs = RevisionViewModel.access$getStartOfDayMs(this.this$0);
                        endOfTodayMs = startOfTodayMs + 86400000L;
                        $this$filter\6 = (Iterable)topics;
                        $i$f$filter\6\483 = false;
                        var19_24 = $this$filter\6;
                        destination\7 = new ArrayList<E>();
                        $i$f$filterTo\7\1053 = false;
                        for (Object element\7 : $this$filterTo\7) {
                            it\8 = (TopicWithSubjectAndChapter)element\7;
                            $i$a$-filter-RevisionViewModel$dashboardMetrics$1$pendingAll$1\8\1054\0 = false;
                            if (!(Intrinsics.areEqual((Object)it\8.getStatus(), (Object)"Complete") != false && it\8.getNextRevisionDateMs() < now)) continue;
                            destination\7.add(element\7);
                        }
                        pendingAll = (List)destination\7;
                        $this$filter\9 = (Iterable)topics;
                        $i$f$filter\9\484 = false;
                        destination\7 = $this$filter\9;
                        destination\10 = new ArrayList<E>();
                        $i$f$filterTo\10\1056 = false;
                        for (Object element\10 : $this$filterTo\10) {
                            it\11 = (TopicWithSubjectAndChapter)element\10;
                            $i$a$-filter-RevisionViewModel$dashboardMetrics$1$todayDue$1\11\1057\0 = false;
                            if (!(Intrinsics.areEqual((Object)it\11.getStatus(), (Object)"Complete") != false && it\11.getNextRevisionDateMs() <= endOfTodayMs)) continue;
                            destination\10.add(element\10);
                        }
                        todayDue = (List)destination\10;
                        $this$filter\12 = (Iterable)topics;
                        $i$f$filter\12\485 = false;
                        destination\10 = $this$filter\12;
                        destination\13 = new ArrayList<E>();
                        $i$f$filterTo\13\1059 = false;
                        for (E element\13 : $this$filterTo\13) {
                            it\14 = (TopicWithSubjectAndChapter)element\13;
                            $i$a$-filter-RevisionViewModel$dashboardMetrics$1$weakTopics$1\14\1060\0 = false;
                            var28_54 = it\14.getMasteryScore();
                            v4 = 0 <= var28_54 ? var28_54 < 41 : false;
                            if (!(v4 != false && Intrinsics.areEqual((Object)it\14.getStatus(), (Object)"Complete") != false)) continue;
                            destination\13.add(element\13);
                        }
                        weakTopics = (List)destination\13;
                        $this$filter\15 = (Iterable)topics;
                        $i$f$filter\15\487 = false;
                        destination\13 = $this$filter\15;
                        destination\16 = new ArrayList<E>();
                        $i$f$filterTo\16\1062 = false;
                        for (T element\16 : $this$filterTo\16) {
                            it\17 = (TopicWithSubjectAndChapter)element\16;
                            $i$a$-filter-RevisionViewModel$dashboardMetrics$1$todayRevisedCount$1\17\1063\0 = false;
                            var29_55 = it\17.getLastRevisionDateMs();
                            v5 = startOfTodayMs <= var29_55 ? var29_55 <= endOfTodayMs : false;
                            if (!v5) continue;
                            destination\16.add(element\16);
                        }
                        todayRevisedCount = ((List)destination\16).size();
                        $this$filter\15 = (Iterable)topics;
                        var32_56 = pendingAll;
                        var31_57 = todayDue;
                        $i$f$filter\18\492 = false;
                        $this$filterTo\16 = $this$filter\18;
                        destination\19 = new ArrayList<E>();
                        $i$f$filterTo\19\1065 = false;
                        for (T element\19 : $this$filterTo\19) {
                            it\20 = (TopicWithSubjectAndChapter)element\19;
                            $i$a$-filter-RevisionViewModel$dashboardMetrics$1$1\20\1066\0 = false;
                            if (!Intrinsics.areEqual((Object)it\20.getStatus(), (Object)"Complete")) continue;
                            destination\19.add(element\19);
                        }
                        var33_58 = (List)destination\19;
                        var34_59 = todayRevisedCount;
                        var35_60 = weakTopics;
                        var36_61 = RevisionViewModel.access$formatTime(this.this$0, todayStudyTimeMs);
                        var37_62 = var33_58;
                        var38_63 = var32_56;
                        var39_64 = var31_57;
                        return new DashboardMetrics(var39_64, var38_63, var37_62, var36_61, var35_60, var34_59);
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(List<TopicWithSubjectAndChapter> p1, String p2, List<StudyLog> p3, Continuation<? super DashboardMetrics> p4) {
                var var5_5 = new /* invalid duplicate definition of identical inner class */;
                var5_5.L$0 = p1;
                var5_5.L$1 = p2;
                var5_5.L$2 = p3;
                return var5_5.invokeSuspend(Unit.INSTANCE);
            }
        })), (Function3)((Function3)new Function3<FlowCollector<? super DashboardMetrics>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super DashboardMetrics> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)new DashboardMetrics(null, null, null, null, null, 0, 63, null));
        this.analyticsMetrics = FlowKt.stateIn((Flow)FlowKt.catch((Flow)FlowKt.combine((Flow)((Flow)this.allTopicsWithDetails), (Flow)((Flow)this.activeExamId), (Flow)((Flow)this.allStudyLogs), (Flow)((Flow)this.currentUserProfile), (Flow)((Flow)this.analyticsTimeFilter), (Function6)((Function6)new Function6<List<? extends TopicWithSubjectAndChapter>, String, List<? extends StudyLog>, UserProfile, String, Continuation<? super AnalyticsMetrics>, Object>(null){
            int label;
            /* synthetic */ Object L$0;
            /* synthetic */ Object L$1;
            /* synthetic */ Object L$2;
            /* synthetic */ Object L$3;
            /* synthetic */ Object L$4;

            /*
             * WARNING - void declaration
             * Enabled aggressive block sorting
             */
            public final Object invokeSuspend(Object $result) {
                void $this$mapNotNullTo\58;
                int n13;
                Long l13;
                Comparable comparable;
                int n2;
                Object object16;
                int n322;
                int n4;
                void $this$sortedByDescending\48;
                Object object2;
                Iterable iterable;
                void $this$sortedByDescending\35;
                Object object3;
                Object object4;
                Map map2;
                int overallProgressPercentage;
                int averageScore;
                List completedTopics;
                int dayStreak;
                void profile;
                int daysCompleted;
                String totalStudyTimeStr;
                long l2;
                void filteredLogs;
                void topics;
                List list = (List)this.L$0;
                String string = (String)this.L$1;
                List list2 = (List)this.L$2;
                UserProfile userProfile = (UserProfile)this.L$3;
                String string2 = (String)this.L$4;
                Object object5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void $this$mapNotNullTo\29;
                        Object object6;
                        Object object7;
                        Iterable iterable2;
                        int n5;
                        int n6;
                        StudyLog studyLog;
                        List list3;
                        void timeFilter;
                        List list4;
                        void allTopics2;
                        void activeId;
                        ResultKt.throwOnFailure((Object)$result);
                        if (((CharSequence)activeId).length() == 0 || Intrinsics.areEqual((Object)activeId, (Object)"all")) {
                            list4 = allTopics2;
                        } else {
                            void $this$filterTo\2;
                            Iterable iterable3 = (Iterable)allTopics2;
                            boolean bl = false;
                            Iterable iterable4 = iterable3;
                            Collection collection = new ArrayList<E>();
                            boolean bl2 = false;
                            for (T t : $this$filterTo\2) {
                                TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)t;
                                boolean bl3 = false;
                                if (!topicWithSubjectAndChapter.getExamIds().contains(activeId)) continue;
                                collection.add(t);
                            }
                            list4 = (List)collection;
                        }
                        topics = list4;
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String todayStrLoc = sdfLocal.format(cal.getTime());
                        cal.set(7, cal.getFirstDayOfWeek());
                        String weekStr = sdfLocal.format(cal.getTime());
                        cal.set(5, 1);
                        String monthStr = sdfLocal.format(cal.getTime());
                        switch (timeFilter) {
                            case "Today": {
                                Iterable iterable5;
                                void logs;
                                Iterable iterable6 = (Iterable)logs;
                                boolean bl = false;
                                Iterable iterable7 = iterable6;
                                Collection collection = new ArrayList<E>();
                                boolean bl4 = false;
                                for (E e : iterable5) {
                                    StudyLog studyLog2 = (StudyLog)e;
                                    boolean bl5 = false;
                                    if (!Intrinsics.areEqual((Object)studyLog2.getDateString(), (Object)todayStrLoc)) continue;
                                    collection.add(e);
                                }
                                list3 = (List)collection;
                                break;
                            }
                            case "Week": {
                                Iterable iterable8;
                                void logs;
                                Iterable iterable9 = (Iterable)logs;
                                boolean bl = false;
                                Iterable iterable5 = iterable9;
                                Collection collection = new ArrayList<E>();
                                boolean bl6 = false;
                                for (E e : iterable8) {
                                    StudyLog studyLog3 = (StudyLog)e;
                                    boolean bl7 = false;
                                    String string3 = studyLog3.getDateString();
                                    Intrinsics.checkNotNull((Object)weekStr);
                                    if (!(string3.compareTo(weekStr) >= 0)) continue;
                                    collection.add(e);
                                }
                                list3 = (List)collection;
                                break;
                            }
                            case "Month": {
                                void $this$filterTo\11;
                                void logs;
                                Iterable iterable10 = (Iterable)logs;
                                boolean bl = false;
                                Iterable iterable8 = iterable10;
                                Collection collection = new ArrayList<E>();
                                boolean bl8 = false;
                                Object object8 = $this$filterTo\11.iterator();
                                while (object8.hasNext()) {
                                    StudyLog studyLog4 = studyLog = object8.next();
                                    boolean bl9 = false;
                                    String string4 = studyLog4.getDateString();
                                    Intrinsics.checkNotNull((Object)monthStr);
                                    if (!(string4.compareTo(monthStr) >= 0)) continue;
                                    collection.add(studyLog);
                                }
                                list3 = (List)collection;
                                break;
                            }
                            default: {
                                void logs;
                                list3 = logs;
                            }
                        }
                        filteredLogs = list3;
                        Iterable bl = (Iterable)filteredLogs;
                        long l3 = 0L;
                        for (Object object8 : bl) {
                            void it\13;
                            studyLog = (StudyLog)object8;
                            long l4 = l3;
                            boolean bl10 = false;
                            l2 = it\13.getStudyTimeMs() + it\13.getRevisionTimeMs();
                            l3 = l4 + l2;
                        }
                        long totalTimeMs = l3;
                        long hours = totalTimeMs / (long)3600000;
                        long mins = totalTimeMs / (long)60000 % (long)60;
                        totalStudyTimeStr = hours + "h " + mins + "m";
                        Iterable iterable11 = (Iterable)filteredLogs;
                        int n7 = 0;
                        if (iterable11 instanceof Collection && ((Collection)iterable11).isEmpty()) {
                            n6 = 0;
                        } else {
                            int n8 = 0;
                            for (T t : iterable11) {
                                StudyLog studyLog5 = (StudyLog)t;
                                boolean bl11 = false;
                                if (!(studyLog5.getStudyTimeMs() > 0L || studyLog5.getRevisionTimeMs() > 0L) || ++n8 >= 0) continue;
                                CollectionsKt.throwCountOverflow();
                            }
                            n6 = n8;
                        }
                        daysCompleted = n6;
                        void v5 = profile;
                        dayStreak = v5 != null ? (n7 = v5.getCurrentStreak()) : 0;
                        Iterable iterable12 = (Iterable)topics;
                        boolean bl12 = false;
                        Iterable iterable13 = iterable12;
                        Collection collection = new ArrayList<E>();
                        boolean bl13 = false;
                        for (Object object9 : iterable13) {
                            TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)object9;
                            boolean bl14 = false;
                            if (!Intrinsics.areEqual((Object)topicWithSubjectAndChapter.getStatus(), (Object)"Complete")) continue;
                            collection.add(object9);
                        }
                        completedTopics = (List)collection;
                        if (!((Collection)completedTopics).isEmpty()) {
                            Iterable iterable14 = completedTopics;
                            boolean bl15 = false;
                            Iterable iterable15 = iterable14;
                            Collection collection2 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)iterable14, (int)10));
                            boolean bl16 = false;
                            for (E e : iterable15) {
                                void object11;
                                TopicWithSubjectAndChapter bl14 = (TopicWithSubjectAndChapter)e;
                                Collection collection3 = collection2;
                                boolean bl17 = false;
                                Integer n9 = Boxing.boxInt((int)object11.getMasteryScore());
                                collection3.add(n9);
                            }
                            n5 = (int)CollectionsKt.averageOfInt((Iterable)((List)collection2));
                        } else {
                            n5 = 0;
                        }
                        averageScore = n5;
                        overallProgressPercentage = !((Collection)topics).isEmpty() ? completedTopics.size() * 100 / topics.size() : 0;
                        Iterable iterable16 = (Iterable)topics;
                        boolean bl18 = false;
                        Iterable bl16 = iterable16;
                        Map destination\242 = new LinkedHashMap<K, V>();
                        boolean bl19 = false;
                        for (T t : iterable2) {
                            Object object10;
                            TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)t;
                            boolean bl20 = false;
                            String string5 = topicWithSubjectAndChapter.getSubjectName();
                            object7 = destination\242;
                            boolean bl21 = false;
                            V v = object7.get(string5);
                            if (v == null) {
                                boolean bl22 = false;
                                List list5 = new ArrayList<E>();
                                object7.put(string5, list5);
                                object10 = list5;
                            } else {
                                object10 = v;
                            }
                            object6 = (List)object10;
                            object6.add(t);
                        }
                        map2 = destination\242;
                        iterable2 = (Iterable)topics;
                        long destination\242 = 0L;
                        for (T t : iterable2) {
                            void it\27;
                            object6 = (TopicWithSubjectAndChapter)t;
                            long l5 = destination\242;
                            boolean bl23 = false;
                            l2 = it\27.getStudyTimeMs() + it\27.getRevisionTimeMs();
                            destination\242 = l5 + l2;
                        }
                        long l6 = destination\242;
                        object4 = map2;
                        boolean bl24 = false;
                        Object object11 = object4;
                        object3 = new ArrayList<E>();
                        boolean bl25 = false;
                        void $this$forEach\30 = $this$mapNotNullTo\29;
                        boolean bl26 = false;
                        object7 = $this$forEach\30.entrySet().iterator();
                        while (object7.hasNext()) {
                            SubjectBreakdown subjectBreakdown;
                            Map.Entry entry;
                            Map.Entry entry2 = entry = (Map.Entry)object7.next();
                            boolean bl27 = false;
                            Map.Entry entry3 = entry2;
                            boolean bl28 = false;
                            String string6 = (String)entry3.getKey();
                            List list6 = (List)entry3.getValue();
                            if (Intrinsics.areEqual((Object)string6, (Object)"Unknown Subject")) {
                                subjectBreakdown = null;
                            } else {
                                Iterable iterable17 = list6;
                                long l7 = 0L;
                                for (T t : iterable17) {
                                    void it\34;
                                    TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)t;
                                    long l8 = l7;
                                    boolean bl29 = false;
                                    long l9 = it\34.getStudyTimeMs() + it\34.getRevisionTimeMs();
                                    l7 = l8 + l9;
                                }
                                long l10 = l7;
                                int n10 = l6 > 0L ? (int)(l10 * (long)100 / l6) : 0;
                                Object object12 = (TopicWithSubjectAndChapter)CollectionsKt.firstOrNull((List)list6);
                                if (object12 == null || (object12 = ((TopicWithSubjectAndChapter)object12).getSubjectColorHex()) == null) {
                                    object12 = "#000000";
                                }
                                Object object13 = object12;
                                long l11 = l10 / (long)3600000;
                                long l12 = l10 / (long)60000 % (long)60;
                                subjectBreakdown = new SubjectBreakdown(string6, n10, (String)object13, l11 + "h " + l12 + "m");
                            }
                            if (subjectBreakdown == null) continue;
                            SubjectBreakdown subjectBreakdown2 = subjectBreakdown;
                            boolean bl30 = false;
                            object3.add(subjectBreakdown2);
                        }
                        break;
                    }
                    default: {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                }
                object4 = (List)object3;
                boolean bl = false;
                List studyTimeBreakdown = CollectionsKt.sortedWith((Iterable)$this$sortedByDescending\35, (Comparator)new Comparator(){

                    /*
                     * WARNING - void declaration
                     */
                    public final int compare(T a, T b) {
                        void it\2;
                        SubjectBreakdown subjectBreakdown = (SubjectBreakdown)b;
                        boolean bl = false;
                        Comparable comparable = Integer.valueOf(subjectBreakdown.getPercentage());
                        subjectBreakdown = (SubjectBreakdown)a;
                        Comparable comparable2 = comparable;
                        boolean bl2 = false;
                        return ComparisonsKt.compareValues((Comparable)comparable2, (Comparable)Integer.valueOf(it\2.getPercentage()));
                    }
                });
                Object object14 = map2;
                boolean bl31 = false;
                object3 = object14;
                Collection destination\3822 = new ArrayList<E>();
                boolean bl322 = false;
                Iterator<T> $this$forEach\392 = iterable;
                boolean n322 = false;
                Object entry = $this$forEach\392.entrySet().iterator();
                while (entry.hasNext()) {
                    SubjectPerformance subjectPerformance;
                    Map.Entry<K, V> entry4;
                    Map.Entry<K, V> entry5 = entry4 = entry.next();
                    boolean bl33 = false;
                    Map.Entry<K, V> bl28 = entry5;
                    boolean bl34 = false;
                    String string7 = (String)bl28.getKey();
                    List list7 = (List)bl28.getValue();
                    if (Intrinsics.areEqual((Object)string7, (Object)"Unknown Subject")) {
                        subjectPerformance = null;
                    } else {
                        String string8;
                        Object object15;
                        int n11;
                        int n12;
                        void $this$filterTo\43;
                        Iterable iterable18 = list7;
                        boolean bl35 = false;
                        Iterable iterable19 = iterable18;
                        Collection collection = new ArrayList<E>();
                        boolean bl36 = false;
                        for (T t : $this$filterTo\43) {
                            TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)t;
                            boolean bl37 = false;
                            if (!Intrinsics.areEqual((Object)topicWithSubjectAndChapter.getStatus(), (Object)"Complete")) continue;
                            collection.add(t);
                        }
                        List list8 = (List)collection;
                        if (!((Collection)list8).isEmpty()) {
                            Iterable iterable20 = list8;
                            boolean bl38 = false;
                            Iterable iterable21 = iterable20;
                            Collection collection4 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)iterable20, (int)10));
                            boolean l13 = false;
                            for (T t : iterable21) {
                                void it\47;
                                TopicWithSubjectAndChapter bl37 = (TopicWithSubjectAndChapter)t;
                                Collection collection5 = collection4;
                                boolean bl39 = false;
                                collection5.add(Boxing.boxInt((int)it\47.getMasteryScore()));
                            }
                            n12 = (int)CollectionsKt.averageOfInt((Iterable)((List)collection4));
                        } else {
                            n12 = n11 = 0;
                        }
                        if ((object15 = (TopicWithSubjectAndChapter)CollectionsKt.firstOrNull((List)list7)) == null || (object15 = ((TopicWithSubjectAndChapter)object15).getSubjectColorHex()) == null) {
                            object15 = "#000000";
                        }
                        object2 = object15;
                        Intrinsics.checkNotNullExpressionValue((Object)StringsKt.take((String)string7, (int)1).toUpperCase(Locale.ROOT), (String)"toUpperCase(...)");
                        subjectPerformance = new SubjectPerformance(string7, n11, (String)object2, string8);
                    }
                    if (subjectPerformance == null) continue;
                    SubjectPerformance subjectPerformance2 = subjectPerformance;
                    boolean n13 = false;
                    destination\3822.add(subjectPerformance2);
                }
                object14 = (List)destination\3822;
                boolean bl40 = false;
                List subjectPerformance = CollectionsKt.sortedWith((Iterable)$this$sortedByDescending\48, (Comparator)new Comparator(){

                    /*
                     * WARNING - void declaration
                     */
                    public final int compare(T a, T b) {
                        void it\2;
                        SubjectPerformance subjectPerformance = (SubjectPerformance)b;
                        boolean bl = false;
                        Comparable comparable = Integer.valueOf(subjectPerformance.getPercentage());
                        subjectPerformance = (SubjectPerformance)a;
                        Comparable comparable2 = comparable;
                        boolean bl2 = false;
                        return ComparisonsKt.compareValues((Comparable)comparable2, (Comparable)Integer.valueOf(it\2.getPercentage()));
                    }
                });
                iterable = (Iterable)filteredLogs;
                long destination\3822 = 0L;
                for (T n322 : iterable) {
                    T t;
                    entry = (StudyLog)n322;
                    long l14 = destination\3822;
                    boolean bl41 = false;
                    l2 = ((StudyLog)t).getStudyTimeMs() + ((StudyLog)t).getRevisionTimeMs();
                    destination\3822 = l14 + l2;
                }
                long calcTotalMs = destination\3822;
                if (calcTotalMs > 0L) {
                    Iterable destination\3822 = (Iterable)filteredLogs;
                    long bl322 = 0L;
                    for (T t : destination\3822) {
                        void studyLog;
                        StudyLog bl41 = (StudyLog)t;
                        long l15 = bl322;
                        boolean bl42 = false;
                        l2 = studyLog.getStudyTimeMs();
                        bl322 = l15 + l2;
                    }
                    n4 = RangesKt.coerceIn((int)((int)((float)bl322 * 100.0f / (float)calcTotalMs)), (int)0, (int)100);
                } else {
                    n4 = 0;
                }
                int productivityScore = n4;
                Iterable bl322 = (Iterable)filteredLogs;
                int $this$forEach\392 = 0;
                for (T t : bl322) {
                    void it\51;
                    StudyLog studyLog = (StudyLog)t;
                    int n14 = $this$forEach\392;
                    boolean bl43 = false;
                    int n15 = it\51.getFocusSessions();
                    $this$forEach\392 = n14 + n15;
                }
                int focusSessions = $this$forEach\392;
                void v14 = profile;
                int coinsEarned = v14 != null ? ($this$forEach\392 = v14.getTotalCoins()) : 0;
                void v15 = profile;
                int totalRevisions = v15 != null ? (n322 = v15.getTotalRevisions()) : 0;
                int completedTopicsCount = completedTopics.size();
                int pendingTopics = topics.size() - completedTopicsCount;
                int retentionRate = averageScore;
                Iterable bl28 = (Iterable)filteredLogs;
                long l16 = 0L;
                for (T n11 : bl28) {
                    void it\52;
                    object2 = (StudyLog)n11;
                    long l17 = l16;
                    boolean bl44 = false;
                    l2 = it\52.getDeepFocusTimeMs();
                    l16 = l17 + l2;
                }
                long deepFocusTimeMs = l16;
                long deepFocusH = deepFocusTimeMs / (long)3600000;
                long deepFocusM = deepFocusTimeMs / (long)60000 % (long)60;
                String deepFocusTimeStr = deepFocusH + "h " + deepFocusM + "m";
                if (focusSessions > 0) {
                    Iterable bl44 = (Iterable)filteredLogs;
                    long l18 = 0L;
                    for (Object object16 : bl44) {
                        void iterator;
                        StudyLog t = (StudyLog)object16;
                        long l19 = l18;
                        boolean bl45 = false;
                        l2 = iterator.getStudyTimeMs();
                        l18 = l19 + l2;
                    }
                    n2 = (int)(l18 / (long)focusSessions / (long)60000);
                } else {
                    n2 = 0;
                }
                int avgSessionMins = n2;
                String averageSessionStr = avgSessionMins + " min";
                object16 = (Iterable)filteredLogs;
                Iterator<T> iterator = object16.iterator();
                if (!iterator.hasNext()) {
                    comparable = null;
                } else {
                    StudyLog studyLog = (StudyLog)iterator.next();
                    boolean bl46 = false;
                    Object object17 = Boxing.boxLong((long)studyLog.getLongestSessionMs());
                    while (iterator.hasNext()) {
                        Object object18 = (StudyLog)iterator.next();
                        boolean bl47 = false;
                        if (object17.compareTo(object18 = (Comparable)Boxing.boxLong((long)((StudyLog)object18).getLongestSessionMs())) >= 0) continue;
                        object17 = object18;
                    }
                    comparable = object17;
                }
                Long l20 = l13 = (Long)comparable;
                long l21 = l20 != null ? l20 : 0L;
                long longestH = l21 / (long)3600000;
                long longestM = l21 / (long)60000 % (long)60;
                String longestSessionStr = longestH + "h " + longestM + "m";
                Iterable bl39 = (Iterable)filteredLogs;
                int subjectPerformance2 = 0;
                for (T l12 : bl39) {
                    void it\56;
                    StudyLog studyLog = (StudyLog)l12;
                    int n16 = subjectPerformance2;
                    boolean bl48 = false;
                    int n17 = it\56.getTotalDistractions();
                    subjectPerformance2 = n16 + n17;
                }
                int distractionCount = subjectPerformance2;
                int currentStreak = dayStreak;
                void v19 = profile;
                int bestStreak = v19 != null ? (n13 = v19.getLongestStreak()) : 0;
                Object object19 = map2;
                boolean bl49 = false;
                Map bl48 = object19;
                Collection collection = new ArrayList<E>();
                boolean bl50 = false;
                Iterable iterable22 = $this$mapNotNullTo\58;
                boolean bl51 = false;
                Iterator<Map.Entry<K, V>> iterator2 = iterable22.entrySet().iterator();
                while (true) {
                    SubjectProgress subjectProgress;
                    block73: {
                        String string9;
                        Object object20;
                        int n18;
                        int n19;
                        Object object21;
                        int n20;
                        float f;
                        int n21;
                        int n22;
                        int n23;
                        List list9;
                        String string10;
                        block75: {
                            int n24;
                            int n25;
                            Object object22;
                            block76: {
                                void $this$mapTo\70;
                                void $this$sortedByDescending\68;
                                int n26;
                                block71: {
                                    Iterable iterable23;
                                    block74: {
                                        int n27;
                                        block72: {
                                            Map.Entry<K, V> entry6;
                                            if (!iterator2.hasNext()) break block71;
                                            Map.Entry<K, V> entry7 = entry6 = iterator2.next();
                                            n26 = 0;
                                            Map.Entry<K, V> entry8 = entry7;
                                            boolean bl52 = false;
                                            string10 = (String)entry8.getKey();
                                            list9 = (List)entry8.getValue();
                                            if (!Intrinsics.areEqual((Object)string10, (Object)"Unknown Subject")) break block72;
                                            subjectProgress = null;
                                            break block73;
                                        }
                                        n23 = list9.size();
                                        Iterable iterable24 = list9;
                                        boolean bl53 = false;
                                        if (iterable24 instanceof Collection && ((Collection)iterable24).isEmpty()) {
                                            n27 = 0;
                                        } else {
                                            int n28 = 0;
                                            for (T t : iterable24) {
                                                TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)t;
                                                boolean bl54 = false;
                                                if (!Intrinsics.areEqual((Object)topicWithSubjectAndChapter.getStatus(), (Object)"Complete") || ++n28 >= 0) continue;
                                                CollectionsKt.throwCountOverflow();
                                            }
                                            n27 = n28;
                                        }
                                        n22 = n27;
                                        n21 = n23 - n22;
                                        f = n23 > 0 ? (float)n22 / (float)n23 : 0.0f;
                                        n20 = n23 > 0 ? n22 * 100 / n23 : 0;
                                        Object object23 = (TopicWithSubjectAndChapter)CollectionsKt.firstOrNull((List)list9);
                                        if (object23 == null || (object23 = ((TopicWithSubjectAndChapter)object23).getSubjectColorHex()) == null) {
                                            object23 = "#000000";
                                        }
                                        object21 = object23;
                                        Intrinsics.checkNotNullExpressionValue((Object)StringsKt.take((String)string10, (int)2).toUpperCase(Locale.ROOT), (String)"toUpperCase(...)");
                                        Iterable bl54 = list9;
                                        int n29 = 0;
                                        for (T t : bl54) {
                                            void it\65;
                                            object22 = (TopicWithSubjectAndChapter)t;
                                            int n30 = n29;
                                            boolean bl55 = false;
                                            int n31 = (int)((it\65.getStudyTimeMs() + it\65.getRevisionTimeMs()) / 60000L);
                                            n29 = n30 + n31;
                                        }
                                        n19 = n29;
                                        iterable23 = list9;
                                        boolean bl56 = false;
                                        if (!(iterable23 instanceof Collection) || !((Collection)iterable23).isEmpty()) break block74;
                                        n25 = 0;
                                        break block75;
                                    }
                                    n24 = 0;
                                    object22 = iterable23.iterator();
                                    break block76;
                                }
                                object19 = (List)collection;
                                boolean bl57 = false;
                                List subjectProgressList = CollectionsKt.sortedWith((Iterable)$this$sortedByDescending\68, (Comparator)new Comparator(){

                                    /*
                                     * WARNING - void declaration
                                     */
                                    public final int compare(T a, T b) {
                                        void it\2;
                                        SubjectProgress subjectProgress = (SubjectProgress)b;
                                        boolean bl = false;
                                        Comparable comparable = Float.valueOf(subjectProgress.getProgress());
                                        subjectProgress = (SubjectProgress)a;
                                        Comparable comparable2 = comparable;
                                        boolean bl2 = false;
                                        return ComparisonsKt.compareValues((Comparable)comparable2, (Comparable)Float.valueOf(it\2.getProgress()));
                                    }
                                });
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                                Iterable iterable25 = (Iterable)RangesKt.downTo((int)6, (int)0);
                                boolean bl58 = false;
                                iterable22 = iterable25;
                                Collection collection6 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)iterable25, (int)10));
                                boolean bl59 = false;
                                Iterator<T> iterator3 = $this$mapTo\70.iterator();
                                while (iterator3.hasNext()) {
                                    long l22;
                                    Object v24;
                                    Calendar calendar;
                                    Collection collection7;
                                    block70: {
                                        void daysAgo\71;
                                        int n33;
                                        n26 = n33 = ((IntIterator)iterator3).nextInt();
                                        collection7 = collection6;
                                        boolean bl60 = false;
                                        calendar = Calendar.getInstance();
                                        calendar.add(6, (int)(-daysAgo\71));
                                        String string11 = sdf.format(calendar.getTime());
                                        Iterable iterable26 = (Iterable)filteredLogs;
                                        for (T t : iterable26) {
                                            StudyLog studyLog = (StudyLog)t;
                                            boolean bl61 = false;
                                            if (!Intrinsics.areEqual((Object)studyLog.getDateString(), (Object)string11)) continue;
                                            v24 = t;
                                            break block70;
                                        }
                                        v24 = null;
                                    }
                                    StudyLog studyLog = v24;
                                    if (studyLog != null) {
                                        StudyLog studyLog6;
                                        boolean bl62 = false;
                                        l22 = studyLog6.getStudyTimeMs() + studyLog6.getRevisionTimeMs();
                                    } else {
                                        l22 = 0L;
                                    }
                                    float f2 = (float)l22 / 3600000.0f;
                                    String string12 = dayFormat.format(calendar.getTime());
                                    Intrinsics.checkNotNullExpressionValue((Object)string12, (String)"format(...)");
                                    DayActivity dayActivity = new DayActivity(string12, f2);
                                    collection7.add(dayActivity);
                                }
                                List weeklyActivity = (List)collection6;
                                Iterable iterable27 = weeklyActivity;
                                boolean bl63 = false;
                                Iterable iterable28 = iterable27;
                                Collection collection8 = new ArrayList<E>(CollectionsKt.collectionSizeOrDefault((Iterable)iterable27, (int)10));
                                boolean bl64 = false;
                                for (T t : iterable28) {
                                    void it\76;
                                    DayActivity bl60 = (DayActivity)t;
                                    Collection collection9 = collection8;
                                    boolean bl65 = false;
                                    Float f3 = Boxing.boxFloat((float)it\76.getHours());
                                    collection9.add(f3);
                                }
                                List dailyHours = (List)collection8;
                                Float f4 = bl63 = CollectionsKt.maxOrNull((Iterable)dailyHours);
                                float maxDailyHours = RangesKt.coerceAtLeast((float)(f4 != null ? f4.floatValue() : 8.0f), (float)8.0f);
                                String targetExamName = "N/A";
                                int n34 = overallProgressPercentage;
                                String rangeStr = (int)((double)n34 * 0.85) + " - " + RangesKt.coerceAtMost((int)((int)((double)n34 * 1.1)), (int)100) + "%";
                                return new AnalyticsMetrics(totalStudyTimeStr, productivityScore, focusSessions, coinsEarned, dailyHours, maxDailyHours, subjectProgressList, totalRevisions, completedTopicsCount, pendingTopics, retentionRate, deepFocusTimeStr, averageSessionStr, longestSessionStr, distractionCount, currentStreak, bestStreak, targetExamName, n34, rangeStr, daysCompleted, dayStreak, averageScore, overallProgressPercentage, 0, 0, studyTimeBreakdown, subjectPerformance, weeklyActivity, 12, 8, 15, 0x3000000, null);
                            }
                            while (object22.hasNext()) {
                                E e = object22.next();
                                TopicWithSubjectAndChapter topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)e;
                                boolean bl66 = false;
                                if (!(Intrinsics.areEqual((Object)topicWithSubjectAndChapter.getStatus(), (Object)"Complete") && topicWithSubjectAndChapter.getNextRevisionDateMs() > 0L && topicWithSubjectAndChapter.getNextRevisionDateMs() <= System.currentTimeMillis()) || ++n24 >= 0) continue;
                                CollectionsKt.throwCountOverflow();
                            }
                            n25 = n18 = n24;
                        }
                        if ((object20 = (TopicWithSubjectAndChapter)CollectionsKt.firstOrNull((List)list9)) == null || (object20 = ((TopicWithSubjectAndChapter)object20).getSubjectId()) == null) {
                            object20 = "";
                        }
                        Object object24 = object20;
                        subjectProgress = new SubjectProgress((String)object24, string10, f, n20 + "%", string9, (String)object21, n23, n22, n21, n19, n18, 0, 0, 6144, null);
                    }
                    if (subjectProgress == null) continue;
                    SubjectProgress subjectProgress2 = subjectProgress;
                    boolean bl67 = false;
                    collection.add(subjectProgress2);
                }
            }

            public final Object invoke(List<TopicWithSubjectAndChapter> p1, String p2, List<StudyLog> p3, UserProfile p4, String p5, Continuation<? super AnalyticsMetrics> p6) {
                var var7_7 = new /* invalid duplicate definition of identical inner class */;
                var7_7.L$0 = p1;
                var7_7.L$1 = p2;
                var7_7.L$2 = p3;
                var7_7.L$3 = p4;
                var7_7.L$4 = p5;
                return var7_7.invokeSuspend(Unit.INSTANCE);
            }
        })), (Function3)((Function3)new Function3<FlowCollector<? super AnalyticsMetrics>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super AnalyticsMetrics> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)new AnalyticsMetrics(null, 0, 0, 0, null, 0.0f, null, 0, 0, 0, 0, null, null, null, 0, 0, 0, null, 0, null, 0, 0, 0, 0, 0, 0, null, null, null, 0, 0, 0, -1, null));
        this._adminAllUsers = StateFlowKt.MutableStateFlow(null);
        this.adminAllUsers = FlowKt.asStateFlow(this._adminAllUsers);
        Flow flow = (Flow)this._adminAllUsers;
        boolean bl = false;
        Flow flow2 = flow;
        boolean bl2 = false;
        boolean bl3 = false;
        this.adminDashboardMetrics = FlowKt.stateIn((Flow)FlowKt.catch((Flow)((Flow)new Flow<AdminDashboardMetrics>(flow2){
            final /* synthetic */ Flow $this_unsafeTransform$inlined;
            {
                this.$this_unsafeTransform$inlined = flow;
            }

            public Object collect(FlowCollector collector, Continuation $completion) {
                Continuation continuation = $completion;
                FlowCollector $this$unsafeTransform_u24lambda_u240 = collector;
                boolean bl = false;
                Object object = this.$this_unsafeTransform$inlined.collect(new FlowCollector($this$unsafeTransform_u24lambda_u240){
                    final /* synthetic */ FlowCollector $this_unsafeFlow;
                    {
                        this.$this_unsafeFlow = $receiver;
                    }

                    /*
                     * Unable to fully structure code
                     */
                    public final Object emit(Object value, Continuation $completion) {
                        if (!($completion instanceof special$$inlined$map$1$2$1)) ** GOTO lbl-1000
                        var3_3 = $completion;
                        if ((var3_3.label & -2147483648) != 0) {
                            var3_3.label -= -2147483648;
                        } else lbl-1000:
                        // 2 sources

                        {
                            $continuation = new ContinuationImpl(this, $completion){
                                /* synthetic */ Object result;
                                int label;
                                Object L$0;
                                final /* synthetic */ special$$inlined$map$1$2 this$0;
                                Object L$1;
                                Object L$2;
                                Object L$3;
                                int I$0;
                                {
                                    this.this$0 = this$0;
                                    super($completion);
                                }

                                public final Object invokeSuspend(Object $result) {
                                    this.result = $result;
                                    this.label |= Integer.MIN_VALUE;
                                    return this.this$0.emit(null, (Continuation)this);
                                }
                            };
                        }
                        $result = $continuation.result;
                        var5_5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                        switch ($continuation.label) {
                            case 0: {
                                ResultKt.throwOnFailure((Object)$result);
                                var6_6 = $continuation;
                                var7_8 = value;
                                $this$map_u24lambda_u245 = this.$this_unsafeFlow;
                                $i$a$-unsafeTransform-FlowKt__TransformKt$map$1\1\49\0 = 0;
                                var10_14 = $this$map_u24lambda_u245;
                                var11_15 = (Continuation)$continuation;
                                users\1 = (List)value;
                                $i$a$-map-RevisionViewModel$adminDashboardMetrics$1\1\50\0 = false;
                                if (users\1 == null) {
                                    v0 = new AdminDashboardMetrics(0, 0, 0, 0, 0, 0, 63, null);
                                } else {
                                    total\1 = users\1.size();
                                    $this$count\2 = users\1;
                                    $i$f$count\2\54 = false;
                                    if ($this$count\2 instanceof Collection && ((Collection)$this$count\2).isEmpty()) {
                                        v1 = 0;
                                    } else {
                                        count\2 = 0;
                                        for (T element\2 : $this$count\2) {
                                            it\3 = (UserProfile)element\2;
                                            $i$a$-count-RevisionViewModel$adminDashboardMetrics$1$active$1\3\57\1 = false;
                                            if (!(it\3.isBanned() == false) || ++count\2 >= 0) continue;
                                            CollectionsKt.throwCountOverflow();
                                        }
                                        v1 = count\2;
                                    }
                                    active\1 = v1;
                                    $this$count\4 = users\1;
                                    $i$f$count\4\59 = false;
                                    if ($this$count\4 instanceof Collection && ((Collection)$this$count\4).isEmpty()) {
                                        v2 = 0;
                                    } else {
                                        count\4 = 0;
                                        for (T element\4 : $this$count\4) {
                                            it\5 = (UserProfile)element\4;
                                            $i$a$-count-RevisionViewModel$adminDashboardMetrics$1$online$1\5\62\1 = false;
                                            if (!(System.currentTimeMillis() - it\5.getLastActiveTimeMs() < 3600000L) || ++count\4 >= 0) continue;
                                            CollectionsKt.throwCountOverflow();
                                        }
                                        v2 = count\4;
                                    }
                                    online\1 = v2;
                                    $i$f$count\4\59 = users\1;
                                    count\4 = 0;
                                    for (Object var20_35 : $i$f$count\4\59) {
                                        it\5 = (UserProfile)var20_35;
                                        var24_46 = count\4;
                                        $i$a$-sumOfInt-RevisionViewModel$adminDashboardMetrics$1$revisions$1\6\64\1 = false;
                                        var25_47 = it\6.getTotalRevisions();
                                        count\4 = var24_46 + var25_47;
                                    }
                                    revisions\1 = count\4;
                                    count\4 = users\1;
                                    var19_33 = 0;
                                    for (E var21_40 : count\4) {
                                        $i$a$-sumOfInt-RevisionViewModel$adminDashboardMetrics$1$revisions$1\6\64\1 = (UserProfile)var21_40;
                                        var24_46 = var19_33;
                                        $i$a$-sumOfInt-RevisionViewModel$adminDashboardMetrics$1$study$1\7\66\1 = false;
                                        var25_47 = it\7.getStudyTimeMinutes();
                                        var19_33 = var24_46 + var25_47;
                                    }
                                    study\1 = var19_33;
                                    var19_34 = users\1;
                                    var20_36 = 0;
                                    for (T var23_45 : var19_34) {
                                        $i$a$-sumOfInt-RevisionViewModel$adminDashboardMetrics$1$study$1\7\66\1 = (UserProfile)var23_45;
                                        var24_46 = var20_36;
                                        $i$a$-sumOfInt-RevisionViewModel$adminDashboardMetrics$1$coins$1\8\67\1 = false;
                                        var25_47 = it\8.getTotalCoins();
                                        var20_36 = var24_46 + var25_47;
                                    }
                                    coins\1 = var20_36;
                                    v0 = new AdminDashboardMetrics(total\1, active\1, online\1, revisions\1, study\1, coins\1);
                                }
                                $continuation.L$0 = SpillingKt.nullOutSpilledVariable((Object)value);
                                $continuation.L$1 = SpillingKt.nullOutSpilledVariable((Object)$completion);
                                $continuation.L$2 = SpillingKt.nullOutSpilledVariable((Object)value);
                                $continuation.L$3 = SpillingKt.nullOutSpilledVariable((Object)$this$map_u24lambda_u245);
                                $continuation.I$0 = $i$a$-unsafeTransform-FlowKt__TransformKt$map$1\1\49\0;
                                $continuation.label = 1;
                                v3 = var10_14.emit((Object)v0, (Continuation)$continuation);
                                if (v3 == var5_5) {
                                    return var5_5;
                                }
                                ** GOTO lbl102
                            }
                            case 1: {
                                $i$a$-unsafeTransform-FlowKt__TransformKt$map$1\1\49\0 = $continuation.I$0;
                                $this$map_u24lambda_u245 = (FlowCollector)$continuation.L$3;
                                value = $continuation.L$2;
                                $completion = $continuation.L$1;
                                value = $continuation.L$0;
                                ResultKt.throwOnFailure((Object)$result);
                                v3 = $result;
lbl102:
                                // 2 sources

                                return Unit.INSTANCE;
                            }
                        }
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                }, $completion);
                if (object == IntrinsicsKt.getCOROUTINE_SUSPENDED()) {
                    return object;
                }
                return Unit.INSTANCE;
            }
        }), (Function3)((Function3)new Function3<FlowCollector<? super AdminDashboardMetrics>, Throwable, Continuation<? super Unit>, Object>(null){
            int label;
            /* synthetic */ Object L$0;

            /*
             * WARNING - void declaration
             */
            public final Object invokeSuspend(Object $result) {
                Throwable throwable = (Throwable)this.L$0;
                IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        void e;
                        ResultKt.throwOnFailure((Object)$result);
                        e.printStackTrace();
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Object invoke(FlowCollector<? super AdminDashboardMetrics> p1, Throwable p2, Continuation<? super Unit> p3) {
                var var4_4 = new /* invalid duplicate definition of identical inner class */;
                var4_4.L$0 = p2;
                return var4_4.invokeSuspend(Unit.INSTANCE);
            }
        })), (CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), (SharingStarted)SharingStarted.Companion.WhileSubscribed$default((SharingStarted.Companion)SharingStarted.Companion, (long)5000L, (long)0L, (int)2, null), (Object)new AdminDashboardMetrics(0, 0, 0, 0, 0, 0, 63, null));
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = this.this$0.repository.getUserIdFlow().collect(new FlowCollector(){

                            /*
                             * Unable to fully structure code
                             */
                            public final Object emit(String uid, Continuation<? super Unit> $completion) {
                                if (!($completion instanceof emit.1)) ** GOTO lbl-1000
                                var4_3 = $completion;
                                if ((var4_3.label & -2147483648) != 0) {
                                    var4_3.label -= -2147483648;
                                } else lbl-1000:
                                // 2 sources

                                {
                                    $continuation = new ContinuationImpl(this, $completion){
                                        Object L$0;
                                        /* synthetic */ Object result;
                                        final /* synthetic */ 1<T> this$0;
                                        int label;
                                        {
                                            this.this$0 = this$0;
                                            super($completion);
                                        }

                                        public final Object invokeSuspend(Object $result) {
                                            this.result = $result;
                                            this.label |= Integer.MIN_VALUE;
                                            return this.this$0.emit(null, (Continuation<Unit>)((Continuation)this));
                                        }
                                    };
                                }
                                $result = $continuation.result;
                                var5_5 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                                switch ($continuation.label) {
                                    case 0: {
                                        ResultKt.throwOnFailure((Object)$result);
                                        if (!(((CharSequence)uid).length() > 0)) break;
                                        $continuation.L$0 = uid;
                                        $continuation.label = 1;
                                        v0 = RevisionViewModel.access$getRepository$p(this$0).checkAndSeedDefaultSyllabus(uid, (Continuation<? super Unit>)$continuation);
                                        if (v0 == var5_5) {
                                            return var5_5;
                                        }
                                        ** GOTO lbl23
                                    }
                                    case 1: {
                                        uid = (String)$continuation.L$0;
                                        ResultKt.throwOnFailure((Object)$result);
                                        v0 = $result;
lbl23:
                                        // 2 sources

                                        $continuation.L$0 = SpillingKt.nullOutSpilledVariable((Object)uid);
                                        $continuation.label = 2;
                                        v1 = RevisionViewModel.access$getRepository$p(this$0).evaluateDailyRewardsAndStreak(uid, (Continuation<? super Unit>)$continuation);
                                        if (v1 == var5_5) {
                                            return var5_5;
                                        }
                                        ** GOTO lbl33
                                    }
                                    case 2: {
                                        uid = (String)$continuation.L$0;
                                        ResultKt.throwOnFailure((Object)$result);
                                        v1 = $result;
lbl33:
                                        // 2 sources

                                        return Unit.INSTANCE;
                                    }
                                }
                                return Unit.INSTANCE;
                                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                            }
                        }, (Continuation)this);
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        Object object2;
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            object2 = $result;
                            return Unit.INSTANCE;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = this.this$0.repository.getAllUsersProfiles().collect(new FlowCollector(){

                            public final Object emit(List<UserProfile> list, Continuation<? super Unit> $completion) {
                                this$0._adminAllUsers.setValue(list);
                                return Unit.INSTANCE;
                            }
                        }, (Continuation)this);
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        Object object2;
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            object2 = $result;
                            return Unit.INSTANCE;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final List<Integer> getRevisionPattern() {
        List<Integer> pattern;
        Object object = (UserProfile)this.currentUserProfile.getValue();
        if (object == null || (object = ((UserProfile)object).getRevisionPattern()) == null) {
            object = CollectionsKt.emptyList();
        }
        return !((Collection)(pattern = object)).isEmpty() ? pattern : RevisionViewModelKt.getDefaultRevisionIntervals();
    }

    @NotNull
    public final Job updateRevisionPattern(@NotNull List<Integer> pattern) {
        Intrinsics.checkNotNullParameter(pattern, (String)"pattern");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, pattern, null){
            Object L$0;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ List<Integer> $pattern;
            {
                this.this$0 = $receiver;
                this.$pattern = $pattern;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        UserProfile userProfile = (UserProfile)this.this$0.getCurrentUserProfile().getValue();
                        if (userProfile == null) {
                            return Unit.INSTANCE;
                        }
                        UserProfile profile = userProfile;
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)profile);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateUserProfile(UserProfile.copy$default(profile, null, null, null, null, null, 0L, 0L, false, null, false, null, 0, 0, 0, 0, 0, null, null, null, 0, 0, false, false, 0, null, this.$pattern, 0, 0.0f, null, 0x1DFFFFFF, null), (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        UserProfile profile = (UserProfile)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final StateFlow<Integer> getUnreadNotificationsCount() {
        return this.unreadNotificationsCount;
    }

    @NotNull
    public final StateFlow<Boolean> isDarkMode() {
        return this.isDarkMode;
    }

    public final void setDarkMode(boolean enabled) {
        this._isDarkMode.setValue((Object)enabled);
    }

    @NotNull
    public final StateFlow<List<Subject>> getAllSubjects() {
        return this.allSubjects;
    }

    @NotNull
    public final StateFlow<UserProfile> getCurrentUserProfile() {
        return this.currentUserProfile;
    }

    @NotNull
    public final StateFlow<RevisionSettings> getRevisionSettings() {
        return this.revisionSettings;
    }

    @NotNull
    public final Job updateRevisionSettings(@NotNull RevisionSettings settings) {
        Intrinsics.checkNotNullParameter((Object)settings, (String)"settings");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, settings, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ RevisionSettings $settings;
            {
                this.this$0 = $receiver;
                this.$settings = $settings;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateRevisionSettings(this.$settings, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final StateFlow<GlobalAppConfig> getGlobalConfig() {
        return this.globalConfig;
    }

    @NotNull
    public final Job updateGlobalConfig(@NotNull Map<String, ? extends Object> updates) {
        Intrinsics.checkNotNullParameter(updates, (String)"updates");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, updates, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ Map<String, Object> $updates;
            {
                this.this$0 = $receiver;
                this.$updates = $updates;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateGlobalConfig(this.$updates, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job resetAllUserStreaks(@NotNull String adminEmail) {
        Intrinsics.checkNotNullParameter((Object)adminEmail, (String)"adminEmail");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, adminEmail, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $adminEmail;
            {
                this.this$0 = $receiver;
                this.$adminEmail = $adminEmail;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).resetAllUserStreaks((Continuation<? super Unit>)((Continuation)this));
                        ** if (v0 != var3_2) goto lbl11
lbl10:
                        // 1 sources

                        return var3_2;
lbl11:
                        // 1 sources

                        ** GOTO lbl17
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl17:
                        // 2 sources

                        this.label = 2;
                        v1 = RevisionViewModel.access$getRepository$p(this.this$0).logSystemAction(RevisionViewModel.access$getRepository$p(this.this$0).getUserId(), this.$adminEmail, "Reset Streaks", "All user streaks reset", (Continuation<? super Unit>)((Continuation)this));
                        ** if (v1 != var3_2) goto lbl21
lbl20:
                        // 1 sources

                        return var3_2;
lbl21:
                        // 1 sources

                        ** GOTO lbl30
                    }
                    case 2: {
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v1 = $result;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
lbl30:
                        // 3 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job resetAllUserCoins(@NotNull String adminEmail) {
        Intrinsics.checkNotNullParameter((Object)adminEmail, (String)"adminEmail");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, adminEmail, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $adminEmail;
            {
                this.this$0 = $receiver;
                this.$adminEmail = $adminEmail;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).resetAllUserCoins((Continuation<? super Unit>)((Continuation)this));
                        ** if (v0 != var3_2) goto lbl11
lbl10:
                        // 1 sources

                        return var3_2;
lbl11:
                        // 1 sources

                        ** GOTO lbl17
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl17:
                        // 2 sources

                        this.label = 2;
                        v1 = RevisionViewModel.access$getRepository$p(this.this$0).logSystemAction(RevisionViewModel.access$getRepository$p(this.this$0).getUserId(), this.$adminEmail, "Reset Coins", "All user coins reset", (Continuation<? super Unit>)((Continuation)this));
                        ** if (v1 != var3_2) goto lbl21
lbl20:
                        // 1 sources

                        return var3_2;
lbl21:
                        // 1 sources

                        ** GOTO lbl30
                    }
                    case 2: {
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v1 = $result;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
lbl30:
                        // 3 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job sendEmergencyBroadcast(@NotNull String title, @NotNull String message, @NotNull String adminEmail) {
        Intrinsics.checkNotNullParameter((Object)title, (String)"title");
        Intrinsics.checkNotNullParameter((Object)message, (String)"message");
        Intrinsics.checkNotNullParameter((Object)adminEmail, (String)"adminEmail");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, title, message, adminEmail, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $title;
            final /* synthetic */ String $message;
            final /* synthetic */ String $adminEmail;
            {
                this.this$0 = $receiver;
                this.$title = $title;
                this.$message = $message;
                this.$adminEmail = $adminEmail;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).sendEmergencyBroadcast(this.$title, this.$message, this.$adminEmail, (Continuation<? super Unit>)((Continuation)this));
                        ** if (v0 != var3_2) goto lbl11
lbl10:
                        // 1 sources

                        return var3_2;
lbl11:
                        // 1 sources

                        ** GOTO lbl17
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl17:
                        // 2 sources

                        this.label = 2;
                        v1 = RevisionViewModel.access$getRepository$p(this.this$0).logSystemAction(RevisionViewModel.access$getRepository$p(this.this$0).getUserId(), this.$adminEmail, "Emergency Broadcast", "Emergency alert sent. Title: " + this.$title, (Continuation<? super Unit>)((Continuation)this));
                        ** if (v1 != var3_2) goto lbl21
lbl20:
                        // 1 sources

                        return var3_2;
lbl21:
                        // 1 sources

                        ** GOTO lbl30
                    }
                    case 2: {
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v1 = $result;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
lbl30:
                        // 3 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final StateFlow<List<Chapter>> getAllChapters() {
        return this.allChapters;
    }

    @NotNull
    public final StateFlow<List<SubjectTest>> getAllSubjectTests() {
        return this.allSubjectTests;
    }

    @NotNull
    public final Job updateChapter(@NotNull Chapter chapter) {
        Intrinsics.checkNotNullParameter((Object)chapter, (String)"chapter");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, chapter, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ Chapter $chapter;
            {
                this.this$0 = $receiver;
                this.$chapter = $chapter;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateChapter(this.$chapter, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job addSubjectTest(@NotNull String subjectId, @NotNull String testName, long dateMs, float marksObtained, float totalMarks, @NotNull String remarks) {
        Intrinsics.checkNotNullParameter((Object)subjectId, (String)"subjectId");
        Intrinsics.checkNotNullParameter((Object)testName, (String)"testName");
        Intrinsics.checkNotNullParameter((Object)remarks, (String)"remarks");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, subjectId, testName, dateMs, marksObtained, totalMarks, remarks, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $subjectId;
            final /* synthetic */ String $testName;
            final /* synthetic */ long $dateMs;
            final /* synthetic */ float $marksObtained;
            final /* synthetic */ float $totalMarks;
            final /* synthetic */ String $remarks;
            {
                this.this$0 = $receiver;
                this.$subjectId = $subjectId;
                this.$testName = $testName;
                this.$dateMs = $dateMs;
                this.$marksObtained = $marksObtained;
                this.$totalMarks = $totalMarks;
                this.$remarks = $remarks;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).insertSubjectTest(this.$subjectId, this.$testName, this.$dateMs, this.$marksObtained, this.$totalMarks, this.$remarks, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job updateSubjectTest(@NotNull SubjectTest test) {
        Intrinsics.checkNotNullParameter((Object)test, (String)"test");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, test, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ SubjectTest $test;
            {
                this.this$0 = $receiver;
                this.$test = $test;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateSubjectTest(this.$test, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job deleteSubjectTest(@NotNull String testId) {
        Intrinsics.checkNotNullParameter((Object)testId, (String)"testId");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, testId, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $testId;
            {
                this.this$0 = $receiver;
                this.$testId = $testId;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).deleteSubjectTest(this.$testId, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final StateFlow<List<Topic>> getAllTopics() {
        return this.allTopics;
    }

    @NotNull
    public final StateFlow<List<TopicWithSubjectAndChapter>> getAllTopicsWithDetails() {
        return this.allTopicsWithDetails;
    }

    @NotNull
    public final StateFlow<Long> getRemainingTimeMs() {
        return this.remainingTimeMs;
    }

    @NotNull
    public final StateFlow<FocusTimerService.TimerState> getTimerState() {
        return this.timerState;
    }

    @NotNull
    public final MutableStateFlow<Boolean> isTimerRunning() {
        return this.isTimerRunning;
    }

    @NotNull
    public final MutableStateFlow<Long> getTimerElapsedMs() {
        return this.timerElapsedMs;
    }

    @NotNull
    public final MutableStateFlow<String> getActiveTimerTopicId() {
        return this.activeTimerTopicId;
    }

    @NotNull
    public final MutableStateFlow<Integer> getActiveTimerSessionCount() {
        return this.activeTimerSessionCount;
    }

    public final void startTimer(@NotNull String topicId) {
        TopicWithSubjectAndChapter topicWithSubjectAndChapter;
        Object object;
        Object v2;
        Intent intent;
        Intent intent2;
        Context context;
        block5: {
            Intrinsics.checkNotNullParameter((Object)topicId, (String)"topicId");
            if (((CharSequence)topicId).length() > 0 && !Intrinsics.areEqual((Object)this.activeTimerTopicId.getValue(), (Object)topicId) && ((Number)this.timerElapsedMs.getValue()).longValue() == 0L) {
                this.activeTimerTopicId.setValue((Object)topicId);
            }
            Context context2 = FirebaseApp.getInstance().getApplicationContext();
            Intrinsics.checkNotNullExpressionValue((Object)context2, (String)"getApplicationContext(...)");
            context = context2;
            intent = intent2 = new Intent(context, FocusTimerService.class);
            boolean bl = false;
            intent.setAction("ACTION_START");
            Object object2 = (UserProfile)this.currentUserProfile.getValue();
            long l = object2 != null && (object2 = ((UserProfile)object2).getFocusSettings()) != null ? (long)((FocusSettings)object2).getStudyModeDurationMins() : 45L;
            intent.putExtra("EXTRA_DURATION", l * (long)60 * 1000L);
            intent.putExtra("EXTRA_TITLE", "Focus Session");
            Iterable iterable = (Iterable)this.allTopicsWithDetails.getValue();
            for (Object t : iterable) {
                TopicWithSubjectAndChapter topicWithSubjectAndChapter2 = (TopicWithSubjectAndChapter)t;
                boolean bl2 = false;
                if (!Intrinsics.areEqual((Object)topicWithSubjectAndChapter2.getTopicId(), (Object)topicId)) continue;
                v2 = t;
                break block5;
            }
            v2 = null;
        }
        if ((object = (topicWithSubjectAndChapter = (TopicWithSubjectAndChapter)v2)) == null || (object = ((TopicWithSubjectAndChapter)object).getTopicName()) == null) {
            object = "Exam Prep";
        }
        intent.putExtra("EXTRA_SUBJECT", (String)object);
        Intent intent3 = intent2;
        try {
            ContextCompat.startForegroundService((Context)context, (Intent)intent3);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static /* synthetic */ void startTimer$default(RevisionViewModel revisionViewModel, String string, int n, Object object) {
        if ((n & 1) != 0) {
            string = "";
        }
        revisionViewModel.startTimer(string);
    }

    public final void pauseTimer() {
        Intent intent;
        Context context = FirebaseApp.getInstance().getApplicationContext();
        Intrinsics.checkNotNullExpressionValue((Object)context, (String)"getApplicationContext(...)");
        Context context2 = context;
        Intent intent2 = intent = new Intent(context2, FocusTimerService.class);
        boolean bl = false;
        intent2.setAction("ACTION_PAUSE");
        Intent intent3 = intent;
        try {
            context2.startService(intent3);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void resetTimer() {
        Intent intent;
        this.activeTimerTopicId.setValue((Object)"");
        this.timerElapsedMs.setValue((Object)0L);
        this.activeTimerSessionCount.setValue((Object)1);
        this.lastAwardedMins = 0;
        Context context = FirebaseApp.getInstance().getApplicationContext();
        Intrinsics.checkNotNullExpressionValue((Object)context, (String)"getApplicationContext(...)");
        Context context2 = context;
        Intent intent2 = intent = new Intent(context2, FocusTimerService.class);
        boolean bl = false;
        intent2.setAction("ACTION_STOP");
        Intent intent3 = intent;
        try {
            context2.startService(intent3);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public final StateFlow<List<StudyLog>> getAllStudyLogs() {
        return this.allStudyLogs;
    }

    @NotNull
    public final StateFlow<List<ExamCountdown>> getAllExams() {
        return this.allExams;
    }

    @NotNull
    public final StateFlow<String> getSelectedExamId() {
        return this.selectedExamId;
    }

    @NotNull
    public final StateFlow<String> getActiveExamId() {
        return this.activeExamId;
    }

    public final void selectExam(@NotNull String examId) {
        Intrinsics.checkNotNullParameter((Object)examId, (String)"examId");
    }

    @NotNull
    public final StateFlow<String> getAnalyticsTimeFilter() {
        return this.analyticsTimeFilter;
    }

    public final void setAnalyticsTimeFilter(@NotNull String filter) {
        Intrinsics.checkNotNullParameter((Object)filter, (String)"filter");
        this._analyticsTimeFilter.setValue((Object)filter);
    }

    public final void insertExam(@NotNull String name, @Nullable Long examDateMs, @Nullable Integer targetStudyHours, @NotNull String category, @NotNull String targetRank) {
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)category, (String)"category");
        Intrinsics.checkNotNullParameter((Object)targetRank, (String)"targetRank");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, name, examDateMs, targetStudyHours, category, targetRank, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $name;
            final /* synthetic */ Long $examDateMs;
            final /* synthetic */ Integer $targetStudyHours;
            final /* synthetic */ String $category;
            final /* synthetic */ String $targetRank;
            {
                this.this$0 = $receiver;
                this.$name = $name;
                this.$examDateMs = $examDateMs;
                this.$targetStudyHours = $targetStudyHours;
                this.$category = $category;
                this.$targetRank = $targetRank;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).insertExam(this.$name, this.$examDateMs, this.$targetStudyHours, this.$category, this.$targetRank, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ void insertExam$default(RevisionViewModel revisionViewModel, String string, Long l, Integer n, String string2, String string3, int n2, Object object) {
        if ((n2 & 8) != 0) {
            string2 = "";
        }
        if ((n2 & 0x10) != 0) {
            string3 = "";
        }
        revisionViewModel.insertExam(string, l, n, string2, string3);
    }

    public final void insertExtendedSyllabus(@NotNull String examId, @NotNull String subjectName, @NotNull String topicName, int totalChapters, int estimatedRevisionTimeMins) {
        Intrinsics.checkNotNullParameter((Object)examId, (String)"examId");
        Intrinsics.checkNotNullParameter((Object)subjectName, (String)"subjectName");
        Intrinsics.checkNotNullParameter((Object)topicName, (String)"topicName");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, examId, subjectName, topicName, totalChapters, estimatedRevisionTimeMins, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $examId;
            final /* synthetic */ String $subjectName;
            final /* synthetic */ String $topicName;
            final /* synthetic */ int $totalChapters;
            final /* synthetic */ int $estimatedRevisionTimeMins;
            {
                this.this$0 = $receiver;
                this.$examId = $examId;
                this.$subjectName = $subjectName;
                this.$topicName = $topicName;
                this.$totalChapters = $totalChapters;
                this.$estimatedRevisionTimeMins = $estimatedRevisionTimeMins;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).insertExtendedSyllabus(this.$examId, this.$subjectName, this.$topicName, this.$totalChapters, this.$estimatedRevisionTimeMins, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void updateExam(@NotNull ExamCountdown exam) {
        Intrinsics.checkNotNullParameter((Object)exam, (String)"exam");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, exam, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ ExamCountdown $exam;
            {
                this.this$0 = $receiver;
                this.$exam = $exam;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateExam(this.$exam, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void deleteExam(@NotNull String examId) {
        Intrinsics.checkNotNullParameter((Object)examId, (String)"examId");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, examId, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $examId;
            {
                this.this$0 = $receiver;
                this.$examId = $examId;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).deleteExam(this.$examId, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final StateFlow<List<CoinTransaction>> getCoinTransactions() {
        return this.coinTransactions;
    }

    @NotNull
    public final StateFlow<List<CoinTransaction>> getAllCoinTransactions() {
        return this.allCoinTransactions;
    }

    @NotNull
    public final StateFlow<List<ReferralRecord>> getReferrals() {
        return this.referrals;
    }

    @NotNull
    public final StateFlow<List<ReferralRecord>> getAllReferrals() {
        return this.allReferrals;
    }

    @NotNull
    public final StateFlow<List<WithdrawalRequest>> getWithdrawalRequests() {
        return this.withdrawalRequests;
    }

    @NotNull
    public final StateFlow<List<WithdrawalRequest>> getAllWithdrawalRequests() {
        return this.allWithdrawalRequests;
    }

    @NotNull
    public final StateFlow<List<AdminLog>> getAdminLogs() {
        return this.adminLogs;
    }

    @NotNull
    public final StateFlow<List<NotificationBroadcast>> getBroadcastAnalytics() {
        return this.broadcastAnalytics;
    }

    public final void sendBroadcastNotification(@NotNull String title, @NotNull String message, @NotNull String audience, @NotNull String adminEmail, @NotNull Function2<? super Boolean, ? super String, Unit> onResult) {
        Intrinsics.checkNotNullParameter((Object)title, (String)"title");
        Intrinsics.checkNotNullParameter((Object)message, (String)"message");
        Intrinsics.checkNotNullParameter((Object)audience, (String)"audience");
        Intrinsics.checkNotNullParameter((Object)adminEmail, (String)"adminEmail");
        Intrinsics.checkNotNullParameter(onResult, (String)"onResult");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, title, message, audience, adminEmail, onResult, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $title;
            final /* synthetic */ String $message;
            final /* synthetic */ String $audience;
            final /* synthetic */ String $adminEmail;
            final /* synthetic */ Function2<Boolean, String, Unit> $onResult;
            {
                this.this$0 = $receiver;
                this.$title = $title;
                this.$message = $message;
                this.$audience = $audience;
                this.$adminEmail = $adminEmail;
                this.$onResult = $onResult;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).sendBroadcastNotification(this.$title, this.$message, this.$audience, this.$adminEmail, (Continuation<? super Unit>)((Continuation)this));
                        ** if (v0 != var3_2) goto lbl11
lbl10:
                        // 1 sources

                        return var3_2;
lbl11:
                        // 1 sources

                        ** GOTO lbl17
                    }
                    case 1: {
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v0 = $result;
lbl17:
                            // 2 sources

                            this.$onResult.invoke((Object)Boxing.boxBoolean((boolean)true), (Object)("Broadcast sent successfully to " + this.$audience));
                        }
                        catch (Exception e) {
                            v1 = Boxing.boxBoolean((boolean)false);
                            v2 = e.getMessage();
                            if (v2 == null) {
                                v2 = "Failed to send broadcast";
                            }
                            this.$onResult.invoke((Object)v1, (Object)v2);
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void markBroadcastAsOpened(@NotNull String broadcastId) {
        Intrinsics.checkNotNullParameter((Object)broadcastId, (String)"broadcastId");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, broadcastId, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $broadcastId;
            {
                this.this$0 = $receiver;
                this.$broadcastId = $broadcastId;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).markBroadcastAsOpened(this.$broadcastId, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void submitReferralCode(@NotNull String code, @NotNull Function1<? super String, Unit> onResult) {
        Intrinsics.checkNotNullParameter((Object)code, (String)"code");
        Intrinsics.checkNotNullParameter(onResult, (String)"onResult");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, code, onResult, null){
            Object L$0;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $code;
            final /* synthetic */ Function1<String, Unit> $onResult;
            {
                this.this$0 = $receiver;
                this.$code = $code;
                this.$onResult = $onResult;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).submitReferralCode(this.$code, (Continuation<? super String>)((Continuation)this));
                        if (v0 == var3_2) {
                            return var3_2;
                        }
                        ** GOTO lbl13
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl13:
                        // 2 sources

                        res = (String)v0;
                        this.$onResult.invoke((Object)res);
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)res);
                        this.label = 2;
                        v1 = RevisionViewModel.access$getRepository$p(this.this$0).evaluateReferralPayouts((Continuation<? super Unit>)((Continuation)this));
                        if (v1 == var3_2) {
                            return var3_2;
                        }
                        ** GOTO lbl26
                    }
                    case 2: {
                        res = (String)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v1 = $result;
lbl26:
                        // 2 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job logSystemAction(@NotNull String action, @NotNull String details) {
        Intrinsics.checkNotNullParameter((Object)action, (String)"action");
        Intrinsics.checkNotNullParameter((Object)details, (String)"details");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, action, details, null){
            Object L$0;
            Object L$1;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $action;
            final /* synthetic */ String $details;
            {
                this.this$0 = $receiver;
                this.$action = $action;
                this.$details = $details;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        String uid = "admin";
                        String email = "admin@o.com";
                        try {
                            Object object2;
                            Object object3 = FirebaseAuth.getInstance().getCurrentUser();
                            if (object3 == null || (object3 = object3.getUid()) == null) {
                                object3 = uid = "admin";
                            }
                            if ((object2 = FirebaseAuth.getInstance().getCurrentUser()) == null || (object2 = object2.getEmail()) == null) {
                                object2 = "admin@o.com";
                            }
                            email = object2;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)uid);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)email);
                        this.label = 1;
                        Object object4 = RevisionViewModel.access$getRepository$p(this.this$0).logSystemAction(uid, email, this.$action, this.$details, (Continuation<? super Unit>)((Continuation)this));
                        if (object4 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        String email = (String)this.L$1;
                        String uid = (String)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object4 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void submitWithdrawalRequest(@NotNull WithdrawalRequest req, @NotNull Function1<? super Boolean, Unit> onResult) {
        Intrinsics.checkNotNullParameter((Object)req, (String)"req");
        Intrinsics.checkNotNullParameter(onResult, (String)"onResult");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, req, onResult, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ WithdrawalRequest $req;
            final /* synthetic */ Function1<Boolean, Unit> $onResult;
            {
                this.this$0 = $receiver;
                this.$req = $req;
                this.$onResult = $onResult;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).submitWithdrawalRequest(this.$req, (Continuation<? super Boolean>)((Continuation)this));
                        if (v0 == var3_2) {
                            return var3_2;
                        }
                        ** GOTO lbl13
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl13:
                        // 2 sources

                        this.$onResult.invoke((Object)Boxing.boxBoolean((boolean)((res = ((Boolean)v0).booleanValue()) != false)));
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void approveWithdrawal(@NotNull String reqId, @NotNull String adminId, @NotNull String adminEmail, @NotNull Function1<? super Boolean, Unit> onResult) {
        Intrinsics.checkNotNullParameter((Object)reqId, (String)"reqId");
        Intrinsics.checkNotNullParameter((Object)adminId, (String)"adminId");
        Intrinsics.checkNotNullParameter((Object)adminEmail, (String)"adminEmail");
        Intrinsics.checkNotNullParameter(onResult, (String)"onResult");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, reqId, adminId, adminEmail, onResult, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $reqId;
            final /* synthetic */ String $adminId;
            final /* synthetic */ String $adminEmail;
            final /* synthetic */ Function1<Boolean, Unit> $onResult;
            {
                this.this$0 = $receiver;
                this.$reqId = $reqId;
                this.$adminId = $adminId;
                this.$adminEmail = $adminEmail;
                this.$onResult = $onResult;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).approveWithdrawal(this.$reqId, this.$adminId, this.$adminEmail, (Continuation<? super Boolean>)((Continuation)this));
                        if (v0 == var3_2) {
                            return var3_2;
                        }
                        ** GOTO lbl13
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl13:
                        // 2 sources

                        this.$onResult.invoke((Object)Boxing.boxBoolean((boolean)((res = ((Boolean)v0).booleanValue()) != false)));
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void rejectWithdrawal(@NotNull String reqId, @NotNull String reason, @NotNull String adminId, @NotNull String adminEmail, @NotNull Function1<? super Boolean, Unit> onResult) {
        Intrinsics.checkNotNullParameter((Object)reqId, (String)"reqId");
        Intrinsics.checkNotNullParameter((Object)reason, (String)"reason");
        Intrinsics.checkNotNullParameter((Object)adminId, (String)"adminId");
        Intrinsics.checkNotNullParameter((Object)adminEmail, (String)"adminEmail");
        Intrinsics.checkNotNullParameter(onResult, (String)"onResult");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, reqId, reason, adminId, adminEmail, onResult, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $reqId;
            final /* synthetic */ String $reason;
            final /* synthetic */ String $adminId;
            final /* synthetic */ String $adminEmail;
            final /* synthetic */ Function1<Boolean, Unit> $onResult;
            {
                this.this$0 = $receiver;
                this.$reqId = $reqId;
                this.$reason = $reason;
                this.$adminId = $adminId;
                this.$adminEmail = $adminEmail;
                this.$onResult = $onResult;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var3_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).rejectWithdrawal(this.$reqId, this.$reason, this.$adminId, this.$adminEmail, (Continuation<? super Boolean>)((Continuation)this));
                        if (v0 == var3_2) {
                            return var3_2;
                        }
                        ** GOTO lbl13
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl13:
                        // 2 sources

                        this.$onResult.invoke((Object)Boxing.boxBoolean((boolean)((res = ((Boolean)v0).booleanValue()) != false)));
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void evaluateReferralPayouts() {
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            {
                this.this$0 = $receiver;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).evaluateReferralPayouts((Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void updateVerificationStatus(boolean emailVerified, boolean mobileVerified) {
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, emailVerified, mobileVerified, null){
            Object L$0;
            Object L$1;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ boolean $emailVerified;
            final /* synthetic */ boolean $mobileVerified;
            {
                this.this$0 = $receiver;
                this.$emailVerified = $emailVerified;
                this.$mobileVerified = $mobileVerified;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var4_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = (UserProfile)this.this$0.getCurrentUserProfile().getValue();
                        if (v0 == null) {
                            return Unit.INSTANCE;
                        }
                        current = v0;
                        updated = UserProfile.copy$default(current, null, null, null, null, null, 0L, 0L, false, null, false, null, 0, 0, 0, 0, 0, null, null, null, 0, 0, this.$emailVerified, this.$mobileVerified, 0, null, null, 0, 0.0f, null, 0x1F9FFFFF, null);
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)current);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)updated);
                        this.label = 1;
                        v1 = RevisionViewModel.access$getRepository$p(this.this$0).updateUserProfile(updated, (Continuation<? super Unit>)((Continuation)this));
                        if (v1 == var4_2) {
                            return var4_2;
                        }
                        ** GOTO lbl22
                    }
                    case 1: {
                        updated = (UserProfile)this.L$1;
                        current = (UserProfile)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v1 = $result;
lbl22:
                        // 2 sources

                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)current);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)updated);
                        this.label = 2;
                        v2 = RevisionViewModel.access$getRepository$p(this.this$0).evaluateReferralPayouts((Continuation<? super Unit>)((Continuation)this));
                        if (v2 == var4_2) {
                            return var4_2;
                        }
                        ** GOTO lbl34
                    }
                    case 2: {
                        updated = (UserProfile)this.L$1;
                        current = (UserProfile)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v2 = $result;
lbl34:
                        // 2 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void awardCoins(int amount, @NotNull String description, @Nullable String sessionId) {
        Intrinsics.checkNotNullParameter((Object)description, (String)"description");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, amount, description, sessionId, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ int $amount;
            final /* synthetic */ String $description;
            final /* synthetic */ String $sessionId;
            {
                this.this$0 = $receiver;
                this.$amount = $amount;
                this.$description = $description;
                this.$sessionId = $sessionId;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).awardCoins(this.$amount, this.$description, this.$sessionId, (Continuation<? super Boolean>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ void awardCoins$default(RevisionViewModel revisionViewModel, int n, String string, String string2, int n2, Object object) {
        if ((n2 & 4) != 0) {
            string2 = null;
        }
        revisionViewModel.awardCoins(n, string, string2);
    }

    public final void recordRevisionEarnings(@NotNull String topicId, @NotNull String topicName, int coins) {
        Intrinsics.checkNotNullParameter((Object)topicId, (String)"topicId");
        Intrinsics.checkNotNullParameter((Object)topicName, (String)"topicName");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, topicId, topicName, coins, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $topicId;
            final /* synthetic */ String $topicName;
            final /* synthetic */ int $coins;
            {
                this.this$0 = $receiver;
                this.$topicId = $topicId;
                this.$topicName = $topicName;
                this.$coins = $coins;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).recordRevisionEarnings(this.$topicId, this.$topicName, this.$coins, (Continuation<? super Boolean>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void recordStreakReward(int streakDays, int coins) {
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, streakDays, coins, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ int $streakDays;
            final /* synthetic */ int $coins;
            {
                this.this$0 = $receiver;
                this.$streakDays = $streakDays;
                this.$coins = $coins;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).recordStreakReward(this.$streakDays, this.$coins, (Continuation<? super Boolean>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void recordAchievementBonus(@NotNull String achievementId, @NotNull String achievementName, int coins) {
        Intrinsics.checkNotNullParameter((Object)achievementId, (String)"achievementId");
        Intrinsics.checkNotNullParameter((Object)achievementName, (String)"achievementName");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, achievementId, achievementName, coins, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $achievementId;
            final /* synthetic */ String $achievementName;
            final /* synthetic */ int $coins;
            {
                this.this$0 = $receiver;
                this.$achievementId = $achievementId;
                this.$achievementName = $achievementName;
                this.$coins = $coins;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).recordAchievementBonus(this.$achievementId, this.$achievementName, this.$coins, (Continuation<? super Boolean>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final StateFlow<DashboardMetrics> getDashboardMetrics() {
        return this.dashboardMetrics;
    }

    @NotNull
    public final StateFlow<AnalyticsMetrics> getAnalyticsMetrics() {
        return this.analyticsMetrics;
    }

    @NotNull
    public final StateFlow<List<UserProfile>> getAdminAllUsers() {
        return this.adminAllUsers;
    }

    @NotNull
    public final StateFlow<AdminDashboardMetrics> getAdminDashboardMetrics() {
        return this.adminDashboardMetrics;
    }

    public final void adminAwardCoins(@NotNull String targetUserId, int amount, @NotNull String description) {
        Intrinsics.checkNotNullParameter((Object)targetUserId, (String)"targetUserId");
        Intrinsics.checkNotNullParameter((Object)description, (String)"description");
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, targetUserId, amount, description, null){
            Object L$0;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $targetUserId;
            final /* synthetic */ int $amount;
            final /* synthetic */ String $description;
            {
                this.this$0 = $receiver;
                this.$targetUserId = $targetUserId;
                this.$amount = $amount;
                this.$description = $description;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        String email = "admin@o.com";
                        try {
                            Object object2 = FirebaseAuth.getInstance().getCurrentUser();
                            if (object2 == null || (object2 = object2.getEmail()) == null) {
                                object2 = "admin@o.com";
                            }
                            email = object2;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)email);
                        this.label = 1;
                        Object object3 = RevisionViewModel.access$getRepository$p(this.this$0).adminAwardCoins(this.$targetUserId, this.$amount, this.$description, email, (Continuation<? super Boolean>)((Continuation)this));
                        if (object3 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        String email = (String)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        Object object3 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @Nullable
    public final Object getUserSubjectsCount(@NotNull String targetUserId, @NotNull Continuation<? super Integer> $completion) {
        return this.repository.getUserSubjectsCount(targetUserId, $completion);
    }

    @Nullable
    public final Object getUserTopicsCount(@NotNull String targetUserId, @NotNull Continuation<? super Integer> $completion) {
        return this.repository.getUserTopicsCount(targetUserId, $completion);
    }

    @NotNull
    public final Flow<List<Subject>> getAdminSubjects(@NotNull String targetUserId) {
        Intrinsics.checkNotNullParameter((Object)targetUserId, (String)"targetUserId");
        return this.repository.getAdminSubjects(targetUserId);
    }

    @NotNull
    public final Flow<List<Topic>> getAdminTopics(@NotNull String targetUserId) {
        Intrinsics.checkNotNullParameter((Object)targetUserId, (String)"targetUserId");
        return this.repository.getAdminTopics(targetUserId);
    }

    @NotNull
    public final Job addSubject(@NotNull String name, @NotNull String colorHex, @NotNull String examId, @NotNull String icon, int totalTopicsExpected) {
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)colorHex, (String)"colorHex");
        Intrinsics.checkNotNullParameter((Object)examId, (String)"examId");
        Intrinsics.checkNotNullParameter((Object)icon, (String)"icon");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, name, colorHex, examId, icon, totalTopicsExpected, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $name;
            final /* synthetic */ String $colorHex;
            final /* synthetic */ String $examId;
            final /* synthetic */ String $icon;
            final /* synthetic */ int $totalTopicsExpected;
            {
                this.this$0 = $receiver;
                this.$name = $name;
                this.$colorHex = $colorHex;
                this.$examId = $examId;
                this.$icon = $icon;
                this.$totalTopicsExpected = $totalTopicsExpected;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).insertSubject(this.$name, this.$colorHex, this.$examId, this.$icon, this.$totalTopicsExpected, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ Job addSubject$default(RevisionViewModel revisionViewModel, String string, String string2, String string3, String string4, int n, int n2, Object object) {
        if ((n2 & 2) != 0) {
            string2 = "#0061A4";
        }
        if ((n2 & 4) != 0) {
            string3 = "";
        }
        if ((n2 & 8) != 0) {
            string4 = "book";
        }
        if ((n2 & 0x10) != 0) {
            n = 0;
        }
        return revisionViewModel.addSubject(string, string2, string3, string4, n);
    }

    @NotNull
    public final Job updateSubject(@NotNull String subjectId, @NotNull String name, @NotNull String colorHex, @NotNull String icon, int totalTopicsExpected) {
        Intrinsics.checkNotNullParameter((Object)subjectId, (String)"subjectId");
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)colorHex, (String)"colorHex");
        Intrinsics.checkNotNullParameter((Object)icon, (String)"icon");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, subjectId, name, colorHex, icon, totalTopicsExpected, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $subjectId;
            final /* synthetic */ String $name;
            final /* synthetic */ String $colorHex;
            final /* synthetic */ String $icon;
            final /* synthetic */ int $totalTopicsExpected;
            {
                this.this$0 = $receiver;
                this.$subjectId = $subjectId;
                this.$name = $name;
                this.$colorHex = $colorHex;
                this.$icon = $icon;
                this.$totalTopicsExpected = $totalTopicsExpected;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateSubject(this.$subjectId, this.$name, this.$colorHex, this.$icon, this.$totalTopicsExpected, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job deleteSubject(@NotNull String id2) {
        Intrinsics.checkNotNullParameter((Object)id2, (String)"id");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, id2, null){
            Object L$0;
            Object L$1;
            Object L$2;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $id;
            {
                this.this$0 = $receiver;
                this.$id = $id;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var13_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        var3_3 = (Iterable)this.this$0.getAllTopicsWithDetails().getValue();
                        var4_4 = this.$id;
                        $i$f$filter\1\748 = false;
                        var6_6 = $this$filter\1;
                        destination\2 = new ArrayList<E>();
                        $i$f$filterTo\2\1049 = false;
                        for (T element\2 : $this$filterTo\2) {
                            it\3 = (TopicWithSubjectAndChapter)element\2;
                            $i$a$-filter-RevisionViewModel$deleteSubject$1$relatedTopics$1\3\1050\0 = false;
                            if (!Intrinsics.areEqual((Object)it\3.getSubjectId(), (Object)var4_4)) continue;
                            destination\2.add(element\2);
                        }
                        relatedTopics = (List)destination\2;
                        var3_3 = relatedTopics.iterator();
lbl20:
                        // 3 sources

                        while (var3_3.hasNext()) {
                            topic = (TopicWithSubjectAndChapter)var3_3.next();
                            this.L$0 = SpillingKt.nullOutSpilledVariable((Object)relatedTopics);
                            this.L$1 = var3_3;
                            this.L$2 = SpillingKt.nullOutSpilledVariable((Object)topic);
                            this.label = 1;
                            v0 = RevisionViewModel.access$getRepository$p(this.this$0).deleteTopic(topic.getTopicId(), (Continuation<? super Unit>)((Continuation)this));
                            if (v0 != var13_2) continue;
                            return var13_2;
                        }
                        break;
                    }
                    case 1: {
                        topic = (TopicWithSubjectAndChapter)this.L$2;
                        var3_3 = (Iterator)this.L$1;
                        relatedTopics = (List)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
                        ** GOTO lbl20
                    }
                }
                this.L$0 = SpillingKt.nullOutSpilledVariable((Object)relatedTopics);
                this.L$1 = null;
                this.L$2 = null;
                this.label = 2;
                v1 = RevisionViewModel.access$getRepository$p(this.this$0).deleteSubject(this.$id, (Continuation<? super Unit>)((Continuation)this));
                if (v1 == var13_2) {
                    return var13_2;
                }
                ** GOTO lbl49
                {
                    case 2: {
                        relatedTopics = (List)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v1 = $result;
lbl49:
                        // 2 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job addChapter(@NotNull String subjectId, @NotNull String name, @NotNull String importance) {
        Intrinsics.checkNotNullParameter((Object)subjectId, (String)"subjectId");
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)importance, (String)"importance");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, subjectId, name, importance, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $subjectId;
            final /* synthetic */ String $name;
            final /* synthetic */ String $importance;
            {
                this.this$0 = $receiver;
                this.$subjectId = $subjectId;
                this.$name = $name;
                this.$importance = $importance;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).insertChapter(this.$subjectId, this.$name, this.$importance, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ Job addChapter$default(RevisionViewModel revisionViewModel, String string, String string2, String string3, int n, Object object) {
        if ((n & 4) != 0) {
            string3 = "Medium";
        }
        return revisionViewModel.addChapter(string, string2, string3);
    }

    @NotNull
    public final Job deleteChapter(@NotNull String id2) {
        Intrinsics.checkNotNullParameter((Object)id2, (String)"id");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, id2, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $id;
            {
                this.this$0 = $receiver;
                this.$id = $id;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).deleteChapter(this.$id, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job addTopic(@NotNull String chapterId, @NotNull String name, @NotNull String notes, @NotNull List<String> tags) {
        Intrinsics.checkNotNullParameter((Object)chapterId, (String)"chapterId");
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)notes, (String)"notes");
        Intrinsics.checkNotNullParameter(tags, (String)"tags");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, chapterId, name, notes, tags, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $chapterId;
            final /* synthetic */ String $name;
            final /* synthetic */ String $notes;
            final /* synthetic */ List<String> $tags;
            {
                this.this$0 = $receiver;
                this.$chapterId = $chapterId;
                this.$name = $name;
                this.$notes = $notes;
                this.$tags = $tags;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionRepository.insertTopic$default(RevisionViewModel.access$getRepository$p(this.this$0), this.$chapterId, this.$name, null, 0, this.$notes, this.$tags, (Continuation)this, 12, null);
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ Job addTopic$default(RevisionViewModel revisionViewModel, String string, String string2, String string3, List list, int n, Object object) {
        if ((n & 8) != 0) {
            list = CollectionsKt.emptyList();
        }
        return revisionViewModel.addTopic(string, string2, string3, list);
    }

    @NotNull
    public final Job addTopicWithSubjectContext(@NotNull String subjectId, @NotNull String name, @NotNull String difficulty, int estimatedRevisionTimeMins, @NotNull String notes, @NotNull List<String> tags) {
        Intrinsics.checkNotNullParameter((Object)subjectId, (String)"subjectId");
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)difficulty, (String)"difficulty");
        Intrinsics.checkNotNullParameter((Object)notes, (String)"notes");
        Intrinsics.checkNotNullParameter(tags, (String)"tags");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, subjectId, name, difficulty, estimatedRevisionTimeMins, notes, tags, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $subjectId;
            final /* synthetic */ String $name;
            final /* synthetic */ String $difficulty;
            final /* synthetic */ int $estimatedRevisionTimeMins;
            final /* synthetic */ String $notes;
            final /* synthetic */ List<String> $tags;
            {
                this.this$0 = $receiver;
                this.$subjectId = $subjectId;
                this.$name = $name;
                this.$difficulty = $difficulty;
                this.$estimatedRevisionTimeMins = $estimatedRevisionTimeMins;
                this.$notes = $notes;
                this.$tags = $tags;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).insertTopicWithSubjectId(this.$subjectId, this.$name, this.$difficulty, this.$estimatedRevisionTimeMins, this.$notes, this.$tags, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ Job addTopicWithSubjectContext$default(RevisionViewModel revisionViewModel, String string, String string2, String string3, int n, String string4, List list, int n2, Object object) {
        if ((n2 & 0x20) != 0) {
            list = CollectionsKt.emptyList();
        }
        return revisionViewModel.addTopicWithSubjectContext(string, string2, string3, n, string4, list);
    }

    @NotNull
    public final Job updateTopicDetails(@NotNull String topicId, @NotNull String name, @NotNull String difficulty, int estimatedRevisionTimeMins, @NotNull String notes, @NotNull List<String> tags) {
        Intrinsics.checkNotNullParameter((Object)topicId, (String)"topicId");
        Intrinsics.checkNotNullParameter((Object)name, (String)"name");
        Intrinsics.checkNotNullParameter((Object)difficulty, (String)"difficulty");
        Intrinsics.checkNotNullParameter((Object)notes, (String)"notes");
        Intrinsics.checkNotNullParameter(tags, (String)"tags");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, topicId, name, difficulty, estimatedRevisionTimeMins, notes, tags, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $topicId;
            final /* synthetic */ String $name;
            final /* synthetic */ String $difficulty;
            final /* synthetic */ int $estimatedRevisionTimeMins;
            final /* synthetic */ String $notes;
            final /* synthetic */ List<String> $tags;
            {
                this.this$0 = $receiver;
                this.$topicId = $topicId;
                this.$name = $name;
                this.$difficulty = $difficulty;
                this.$estimatedRevisionTimeMins = $estimatedRevisionTimeMins;
                this.$notes = $notes;
                this.$tags = $tags;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateTopicDetails(this.$topicId, this.$name, this.$difficulty, this.$estimatedRevisionTimeMins, this.$notes, this.$tags, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public static /* synthetic */ Job updateTopicDetails$default(RevisionViewModel revisionViewModel, String string, String string2, String string3, int n, String string4, List list, int n2, Object object) {
        if ((n2 & 0x20) != 0) {
            list = CollectionsKt.emptyList();
        }
        return revisionViewModel.updateTopicDetails(string, string2, string3, n, string4, list);
    }

    @NotNull
    public final Job deleteTopic(@NotNull String id2) {
        Intrinsics.checkNotNullParameter((Object)id2, (String)"id");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, id2, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $id;
            {
                this.this$0 = $receiver;
                this.$id = $id;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).deleteTopic(this.$id, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job updateTopicCompletion(@NotNull String topicId) {
        Intrinsics.checkNotNullParameter((Object)topicId, (String)"topicId");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, topicId, null){
            Object L$0;
            Object L$1;
            Object L$2;
            long J$0;
            long J$1;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $topicId;
            {
                this.this$0 = $receiver;
                this.$topicId = $topicId;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var11_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        var4_3 = (Iterable)this.this$0.getAllTopics().getValue();
                        var5_4 = this.$topicId;
                        var6_6 = var4_3;
                        for (T var8_10 : var6_6) {
                            it\2 = (Topic)var8_10;
                            $i$a$-find-RevisionViewModel$updateTopicCompletion$1$topic$1\2\780\0 = false;
                            if (!Intrinsics.areEqual((Object)it\2.getId(), (Object)var5_4)) continue;
                            v0 = var8_10;
                            ** GOTO lbl15
                        }
                        v0 = null;
lbl15:
                        // 2 sources

                        v1 = v0;
                        if (v1 == null) {
                            return Unit.INSTANCE;
                        }
                        topic = v1;
                        now = System.currentTimeMillis();
                        settings = (RevisionSettings)this.this$0.getRevisionSettings().getValue();
                        nextDate = now + (long)settings.getFirstRevisionDays() * 86400000L;
                        updated = Topic.copy$default(topic, null, null, null, null, null, null, null, 0, "Complete", 0L, 0L, 1, now, nextDate, RevisionViewModel.access$calculateMastery(this.this$0, 1, true), null, null, 100095, null);
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)topic);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)settings);
                        this.L$2 = SpillingKt.nullOutSpilledVariable((Object)updated);
                        this.J$0 = now;
                        this.J$1 = nextDate;
                        this.label = 1;
                        v2 = RevisionViewModel.access$getRepository$p(this.this$0).updateTopic(updated, (Continuation<? super Unit>)((Continuation)this));
                        if (v2 == var11_2) {
                            return var11_2;
                        }
                        ** GOTO lbl41
                    }
                    case 1: {
                        nextDate = this.J$1;
                        now = this.J$0;
                        updated = (Topic)this.L$2;
                        settings = (RevisionSettings)this.L$1;
                        topic = (Topic)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v2 = $result;
lbl41:
                        // 2 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job recordTimerSession(@NotNull String topicId, boolean isRevision, long durationMs) {
        Intrinsics.checkNotNullParameter((Object)topicId, (String)"topicId");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, isRevision, durationMs, topicId, null){
            Object L$0;
            Object L$1;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ boolean $isRevision;
            final /* synthetic */ long $durationMs;
            final /* synthetic */ String $topicId;
            {
                this.this$0 = $receiver;
                this.$isRevision = $isRevision;
                this.$durationMs = $durationMs;
                this.$topicId = $topicId;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var16_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        v0 = RevisionViewModel.access$getRepository$p(this.this$0).recordStudyTime(RevisionViewModel.access$currentDayString(this.this$0), this.$isRevision, this.$durationMs, (Continuation<? super Unit>)((Continuation)this));
                        if (v0 == var16_2) {
                            return var16_2;
                        }
                        ** GOTO lbl13
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = $result;
lbl13:
                        // 2 sources

                        var4_3 = (Iterable)this.this$0.getAllTopics().getValue();
                        var5_5 = this.$topicId;
                        var6_6 = var4_3;
                        for (T var8_9 : var6_6) {
                            it\2 = (Topic)var8_9;
                            $i$a$-find-RevisionViewModel$recordTimerSession$1$topic$1\2\797\0 = false;
                            if (!Intrinsics.areEqual((Object)it\2.getId(), (Object)var5_5)) continue;
                            v1 = var8_9;
                            ** GOTO lbl23
                        }
                        v1 = null;
lbl23:
                        // 2 sources

                        v2 = v1;
                        if (v2 == null) {
                            return Unit.INSTANCE;
                        }
                        topic = v2;
                        if (this.$isRevision) {
                            revCount = topic.getRevisionCount() + 1;
                            settings = (RevisionSettings)this.this$0.getRevisionSettings().getValue();
                            var7_7 = new Integer[]{Boxing.boxInt((int)settings.getFirstRevisionDays()), Boxing.boxInt((int)settings.getSecondRevisionDays()), Boxing.boxInt((int)settings.getThirdRevisionDays()), Boxing.boxInt((int)settings.getFourthRevisionDays()), Boxing.boxInt((int)settings.getFifthRevisionDays())};
                            intervals = CollectionsKt.listOf((Object[])var7_7);
                            nextIntervalIndex = RangesKt.coerceAtMost((int)(revCount - 1), (int)CollectionsKt.getLastIndex((List)intervals));
                            daysMulti = ((Number)intervals.get(nextIntervalIndex)).intValue();
                            nextDate = System.currentTimeMillis() + (long)daysMulti * 86400000L;
                            var11_16 = topic.getRevisionTimeMs() + this.$durationMs;
                            var13_17 = System.currentTimeMillis();
                            var15_18 = RevisionViewModel.access$calculateMastery(this.this$0, revCount, true);
                            v3 = Topic.copy$default(topic, null, null, null, null, null, null, null, 0, "Complete", 0L, var11_16, revCount, var13_17, nextDate, var15_18, null, null, 99071, null);
                        } else {
                            v3 = Topic.copy$default(topic, null, null, null, null, null, null, null, 0, null, topic.getStudyTimeMs() + this.$durationMs, 0L, 0, 0L, 0L, 0, null, null, 130559, null);
                        }
                        updated = v3;
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)topic);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)updated);
                        this.label = 2;
                        v4 = RevisionViewModel.access$getRepository$p(this.this$0).updateTopic(updated, (Continuation<? super Unit>)((Continuation)this));
                        if (v4 == var16_2) {
                            return var16_2;
                        }
                        ** GOTO lbl54
                    }
                    case 2: {
                        updated = (Topic)this.L$1;
                        topic = (Topic)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v4 = $result;
lbl54:
                        // 2 sources

                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    private final String currentDayString() {
        String string = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Intrinsics.checkNotNullExpressionValue((Object)string, (String)"format(...)");
        return string;
    }

    private final long getStartOfDayMs() {
        Calendar cal = Calendar.getInstance();
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    private final String formatTime(long ms) {
        long secs = ms / (long)1000;
        long h = secs / (long)3600;
        long m = secs % (long)3600 / (long)60;
        return h > 0L ? h + "h " + m + "m" : m + "m";
    }

    private final int calculateMastery(int revisions, boolean consistency) {
        int score = RangesKt.coerceAtMost((int)(revisions * 10), (int)70);
        if (consistency) {
            score += 15;
        }
        return RangesKt.coerceIn((int)score, (int)0, (int)100);
    }

    @NotNull
    public final Job banUser(@NotNull String targetUserId, boolean isBanned, @NotNull String reason) {
        Intrinsics.checkNotNullParameter((Object)targetUserId, (String)"targetUserId");
        Intrinsics.checkNotNullParameter((Object)reason, (String)"reason");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, targetUserId, isBanned, reason, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $targetUserId;
            final /* synthetic */ boolean $isBanned;
            final /* synthetic */ String $reason;
            {
                this.this$0 = $receiver;
                this.$targetUserId = $targetUserId;
                this.$isBanned = $isBanned;
                this.$reason = $reason;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).banUser(this.$targetUserId, this.$isBanned, this.$reason, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job deleteUser(@NotNull String targetUserId) {
        Intrinsics.checkNotNullParameter((Object)targetUserId, (String)"targetUserId");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, targetUserId, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $targetUserId;
            {
                this.this$0 = $receiver;
                this.$targetUserId = $targetUserId;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).deleteUser(this.$targetUserId, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job updateUserProfile(@NotNull UserProfile profile) {
        Intrinsics.checkNotNullParameter((Object)profile, (String)"profile");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, profile, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ UserProfile $profile;
            {
                this.this$0 = $receiver;
                this.$profile = $profile;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateUserProfile(this.$profile, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job updateTopic(@NotNull Topic topic) {
        Intrinsics.checkNotNullParameter((Object)topic, (String)"topic");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, topic, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ Topic $topic;
            {
                this.this$0 = $receiver;
                this.$topic = $topic;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).updateTopic(this.$topic, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job saveCustomRevisionPlan(@NotNull String topicId, @NotNull List<Integer> intervals) {
        Intrinsics.checkNotNullParameter((Object)topicId, (String)"topicId");
        Intrinsics.checkNotNullParameter(intervals, (String)"intervals");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, topicId, intervals, null){
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $topicId;
            final /* synthetic */ List<Integer> $intervals;
            {
                this.this$0 = $receiver;
                this.$topicId = $topicId;
                this.$intervals = $intervals;
                super(2, $completion);
            }

            /*
             * Enabled force condition propagation
             * Lifted jumps to return sites
             */
            public final Object invokeSuspend(Object $result) {
                Object object = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        this.label = 1;
                        Object object2 = RevisionViewModel.access$getRepository$p(this.this$0).saveCustomRevisionPlan(this.$topicId, this.$intervals, (Continuation<? super Unit>)((Continuation)this));
                        if (object2 != object) return Unit.INSTANCE;
                        return object;
                    }
                    case 1: {
                        ResultKt.throwOnFailure((Object)$result);
                        Object object2 = $result;
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    public final void checkAndTriggerNotifications(@NotNull Context context) {
        Intrinsics.checkNotNullParameter((Object)context, (String)"context");
        String uid = this.repository.getUserId();
        if (((CharSequence)uid).length() == 0) {
            return;
        }
        BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(this, uid, context, null){
            long J$0;
            long J$1;
            long J$2;
            long J$3;
            Object L$0;
            Object L$1;
            Object L$2;
            Object L$3;
            int label;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ String $uid;
            final /* synthetic */ Context $context;
            {
                this.this$0 = $receiver;
                this.$uid = $uid;
                this.$context = $context;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             */
            public final Object invokeSuspend(Object $result) {
                var25_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                block3 : switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        now = System.currentTimeMillis();
                        startOfTodayMs = RevisionViewModel.access$getStartOfDayMs(this.this$0);
                        endOfTodayMs = startOfTodayMs + 86400000L;
                        $this$filter\1 = (Iterable)this.this$0.getAllTopics().getValue();
                        $i$f$filter\1\883 = false;
                        var11_10 = $this$filter\1;
                        destination\2 = new ArrayList<E>();
                        $i$f$filterTo\2\1049 = false;
                        for (T element\2 : $this$filterTo\2) {
                            it\3 = (Topic)element\2;
                            $i$a$-filter-RevisionViewModel$checkAndTriggerNotifications$1$completedTopics$1\3\1050\0 = false;
                            if (!Intrinsics.areEqual((Object)it\3.getStatus(), (Object)"Complete")) continue;
                            destination\2.add(element\2);
                        }
                        completedTopics = (List)destination\2;
                        v0 = FirebaseFirestore.getInstance();
                        Intrinsics.checkNotNullExpressionValue((Object)v0, (String)"getInstance(...)");
                        db = v0;
                        var10_9 = completedTopics.iterator();
                        break;
                    }
                    case 1: {
                        dueDate = this.J$3;
                        endOfTodayMs = this.J$2;
                        startOfTodayMs = this.J$1;
                        now = this.J$0;
                        topic = (Topic)this.L$3;
                        var10_9 = (Iterator<E>)this.L$2;
                        db = (FirebaseFirestore)this.L$1;
                        completedTopics = (List)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v1 = $result;
                        while (true) {
                            snapshotObj = (QuerySnapshot)v1;
                            v2 = snapshotObj.getDocuments();
                            Intrinsics.checkNotNullExpressionValue((Object)v2, (String)"getDocuments(...)");
                            $this$any\4 = v2;
                            $i$f$any\4\896 = false;
                            if (!($this$any\4 instanceof Collection) || !((Collection)$this$any\4).isEmpty()) ** GOTO lbl50
                            v3 = false;
                            ** GOTO lbl61
lbl50:
                            // 2 sources

                            for (T element\4 : $this$any\4) {
                                doc\5 = (DocumentSnapshot)element\4;
                                $i$a$-any-RevisionViewModel$checkAndTriggerNotifications$1$notifiedToday$1\5\1053\0 = false;
                                var22_24 = doc\5.get("timestampMs");
                                v4 = var22_24 instanceof Number != false ? (Number)var22_24 : null;
                                v5 = ts\5 = v4 != null ? v4.longValue() : 0L;
                                v6 = startOfTodayMs <= ts\5 ? ts\5 <= endOfTodayMs : false;
                                if (!v6) continue;
                                v3 = true;
                                ** GOTO lbl61
                            }
                            v3 = false;
lbl61:
                            // 3 sources

                            if (notifiedToday = v3) break block3;
                            RevisionViewModel.access$showRealLocalNotification(this.this$0, this.$context, topic.getId(), "\ud83d\udcda Revision Due Today", topic.getName() + " revision is scheduled today. Tap to revise now.", "DUE");
                            break block3;
                            break;
                        }
                    }
                    case 2: {
                        dueDate = this.J$3;
                        endOfTodayMs = this.J$2;
                        startOfTodayMs = this.J$1;
                        now = this.J$0;
                        topic = (Topic)this.L$3;
                        var10_9 = (Iterator)this.L$2;
                        db = (FirebaseFirestore)this.L$1;
                        completedTopics = (List)this.L$0;
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v7 = $result;
                            while (true) {
                                snapshotObj = (QuerySnapshot)v7;
                                v8 = snapshotObj.getDocuments();
                                Intrinsics.checkNotNullExpressionValue((Object)v8, (String)"getDocuments(...)");
                                $this$any\6 = v8;
                                $i$f$any\6\911 = false;
                                if (!($this$any\6 instanceof Collection) || !((Collection)$this$any\6).isEmpty()) ** GOTO lbl86
                                v9 = false;
                                ** GOTO lbl97
lbl86:
                                // 2 sources

                                for (T element\6 : $this$any\6) {
                                    doc\7 = (DocumentSnapshot)element\6;
                                    $i$a$-any-RevisionViewModel$checkAndTriggerNotifications$1$notifiedMissedToday$1\7\1056\0 = false;
                                    var22_24 = doc\7.get("timestampMs");
                                    v10 = var22_24 instanceof Number != false ? (Number)var22_24 : null;
                                    v11 = ts\7 = v10 != null ? v10.longValue() : 0L;
                                    v12 = startOfTodayMs <= ts\7 ? ts\7 <= endOfTodayMs : false;
                                    if (!v12) continue;
                                    v9 = true;
                                    ** GOTO lbl97
                                }
                                v9 = false;
lbl97:
                                // 3 sources

                                if (notifiedMissedToday = v9) break block3;
                                RevisionViewModel.access$showRealLocalNotification(this.this$0, this.$context, topic.getId(), "\u26a0 Revision Missed", "You missed your " + topic.getName() + " revision yesterday. Revise now.", "MISSED");
                                break block3;
                                break;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return Unit.INSTANCE;
                        }
                    }
                    default: {
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                    }
                }
                while (var10_9.hasNext()) {
                    topic = (Topic)var10_9.next();
                    dueDate = topic.getNextRevisionDateMs();
                    v13 = startOfTodayMs <= dueDate ? dueDate <= endOfTodayMs : false;
                    if (v13) {
                        v14 = db.collection("users").document(this.$uid).collection("notifications").whereEqualTo("topicId", (Object)topic.getId()).whereEqualTo("type", (Object)"DUE").get();
                        Intrinsics.checkNotNullExpressionValue((Object)v14, (String)"get(...)");
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)completedTopics);
                        this.L$1 = db;
                        this.L$2 = var10_9;
                        this.L$3 = topic;
                        this.J$0 = now;
                        this.J$1 = startOfTodayMs;
                        this.J$2 = endOfTodayMs;
                        this.J$3 = dueDate;
                        this.label = 1;
                        if ((v1 = TasksKt.await((Task)v14, (Continuation)((Continuation)this))) != var25_2) ** continue;
                        return var25_2;
                    }
                    if (dueDate >= startOfTodayMs) continue;
                    v15 = db.collection("users").document(this.$uid).collection("notifications").whereEqualTo("topicId", (Object)topic.getId()).whereEqualTo("type", (Object)"MISSED").get();
                    Intrinsics.checkNotNullExpressionValue((Object)v15, (String)"get(...)");
                    this.L$0 = SpillingKt.nullOutSpilledVariable((Object)completedTopics);
                    this.L$1 = db;
                    this.L$2 = var10_9;
                    this.L$3 = topic;
                    this.J$0 = now;
                    this.J$1 = startOfTodayMs;
                    this.J$2 = endOfTodayMs;
                    this.J$3 = dueDate;
                    this.label = 2;
                    if ((v7 = TasksKt.await((Task)v15, (Continuation)((Continuation)this))) != var25_2) ** continue;
                    return var25_2;
                }
                return Unit.INSTANCE;
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    private final void showRealLocalNotification(Context context, String topicId, String title, String msg, String type) {
        try {
            Intent intent;
            Object object = context.getSystemService("notification");
            Intrinsics.checkNotNull((Object)object, (String)"null cannot be cast to non-null type android.app.NotificationManager");
            NotificationManager notificationManager = (NotificationManager)object;
            String channelId = "revision_space_alerts";
            if (Build.VERSION.SDK_INT >= 26) {
                Intent intent2 = intent = new NotificationChannel(channelId, (CharSequence)"Revision Alerts", 4);
                boolean bl = false;
                intent2.setDescription("Channel for revision and spacing alerts");
                Intent channel = intent;
                notificationManager.createNotificationChannel((NotificationChannel)channel);
            }
            Intent intent3 = intent = new Intent(context, MainActivity.class);
            boolean bl = false;
            intent3.setFlags(0x10008000);
            Intent intent4 = intent;
            PendingIntent pendingIntent = PendingIntent.getActivity((Context)context, (int)(topicId.hashCode() + type.hashCode()), (Intent)intent4, (int)0xC000000);
            Notification notification = new NotificationCompat.Builder(context, channelId).setSmallIcon(17301659).setContentTitle((CharSequence)title).setContentText((CharSequence)msg).setPriority(1).setContentIntent(pendingIntent).setAutoCancel(true).build();
            Intrinsics.checkNotNullExpressionValue((Object)notification, (String)"build(...)");
            Notification notification2 = notification;
            notificationManager.notify(topicId.hashCode() + type.hashCode(), notification2);
            String uid = this.repository.getUserId();
            if (((CharSequence)uid).length() > 0) {
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                Intrinsics.checkNotNullExpressionValue((Object)firebaseFirestore, (String)"getInstance(...)");
                FirebaseFirestore db = firebaseFirestore;
                DocumentReference documentReference = db.collection("users").document(uid).collection("notifications").document();
                Intrinsics.checkNotNullExpressionValue((Object)documentReference, (String)"document(...)");
                DocumentReference notifRef = documentReference;
                Pair[] pairArray = new Pair[]{TuplesKt.to((Object)"id", (Object)notifRef.getId()), TuplesKt.to((Object)"topicId", (Object)topicId), TuplesKt.to((Object)"title", (Object)title), TuplesKt.to((Object)"message", (Object)msg), TuplesKt.to((Object)"timestampMs", (Object)System.currentTimeMillis()), TuplesKt.to((Object)"isRead", (Object)false), TuplesKt.to((Object)"type", (Object)type)};
                notifRef.set((Object)MapsKt.mapOf((Pair[])pairArray));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public final Job uploadProfileImage(@NotNull Uri uri, @NotNull Function1<? super String, Unit> onSuccess, @NotNull Function1<? super Exception, Unit> onFailure) {
        Intrinsics.checkNotNullParameter((Object)uri, (String)"uri");
        Intrinsics.checkNotNullParameter(onSuccess, (String)"onSuccess");
        Intrinsics.checkNotNullParameter(onFailure, (String)"onFailure");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(onFailure, uri, this, onSuccess, null){
            Object L$0;
            Object L$1;
            Object L$2;
            int label;
            final /* synthetic */ Function1<Exception, Unit> $onFailure;
            final /* synthetic */ Uri $uri;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ Function1<String, Unit> $onSuccess;
            {
                this.$onFailure = $onFailure;
                this.$uri = $uri;
                this.this$0 = $receiver;
                this.$onSuccess = $onSuccess;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var8_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = FirebaseAuth.getInstance().getCurrentUser();
                        v1 = uid = v0 != null ? v0.getUid() : null;
                        if (uid == null) {
                            this.$onFailure.invoke((Object)new Exception("User not logged in"));
                            return Unit.INSTANCE;
                        }
                        v2 = FirebaseStorage.getInstance().getReference();
                        Intrinsics.checkNotNullExpressionValue((Object)v2, (String)"getReference(...)");
                        storageRef = v2;
                        v3 = storageRef.child("users/" + uid + "/profile.jpg");
                        Intrinsics.checkNotNullExpressionValue((Object)v3, (String)"child(...)");
                        profileImageRef = v3;
                        v4 = profileImageRef.putFile(this.$uri);
                        Intrinsics.checkNotNullExpressionValue((Object)v4, (String)"putFile(...)");
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)uid);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)storageRef);
                        this.L$2 = profileImageRef;
                        this.label = 1;
                        v5 = TasksKt.await((Task)((Task)v4), (Continuation)((Continuation)this));
                        ** if (v5 != var8_2) goto lbl28
lbl27:
                        // 1 sources

                        return var8_2;
lbl28:
                        // 1 sources

                        ** GOTO lbl37
                    }
                    case 1: {
                        profileImageRef = (StorageReference)this.L$2;
                        storageRef = (StorageReference)this.L$1;
                        uid = (String)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v5 = $result;
lbl37:
                        // 2 sources

                        v6 = profileImageRef.getDownloadUrl();
                        Intrinsics.checkNotNullExpressionValue((Object)v6, (String)"getDownloadUrl(...)");
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)uid);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)storageRef);
                        this.L$2 = SpillingKt.nullOutSpilledVariable((Object)profileImageRef);
                        this.label = 2;
                        v7 = TasksKt.await((Task)v6, (Continuation)((Continuation)this));
                        ** if (v7 != var8_2) goto lbl46
lbl45:
                        // 1 sources

                        return var8_2;
lbl46:
                        // 1 sources

                        ** GOTO lbl55
                    }
                    case 2: {
                        profileImageRef = (StorageReference)this.L$2;
                        storageRef = (StorageReference)this.L$1;
                        uid = (String)this.L$0;
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v7 = $result;
lbl55:
                            // 2 sources

                            v8 = ((Uri)v7).toString();
                            Intrinsics.checkNotNullExpressionValue((Object)v8, (String)"toString(...)");
                            downloadUrl = v8;
                            current = (UserProfile)this.this$0.getCurrentUserProfile().getValue();
                            if (current != null) {
                                updated = UserProfile.copy$default(current, null, null, null, null, null, 0L, 0L, false, null, false, null, 0, 0, 0, 0, 0, null, null, null, 0, 0, false, false, 0, downloadUrl, null, 0, 0.0f, null, 0x1EFFFFFF, null);
                                this.this$0.updateUserProfile(updated);
                            }
                            this.$onSuccess.invoke((Object)downloadUrl);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            this.$onFailure.invoke((Object)e);
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    @NotNull
    public final Job uploadProfileImageBytes(@NotNull byte[] bytes, @NotNull Function1<? super String, Unit> onSuccess, @NotNull Function1<? super Exception, Unit> onFailure) {
        Intrinsics.checkNotNullParameter((Object)bytes, (String)"bytes");
        Intrinsics.checkNotNullParameter(onSuccess, (String)"onSuccess");
        Intrinsics.checkNotNullParameter(onFailure, (String)"onFailure");
        return BuildersKt.launch$default((CoroutineScope)ViewModelKt.getViewModelScope((ViewModel)this), null, null, (Function2)((Function2)new Function2<CoroutineScope, Continuation<? super Unit>, Object>(onFailure, bytes, this, onSuccess, null){
            Object L$0;
            Object L$1;
            Object L$2;
            int label;
            final /* synthetic */ Function1<Exception, Unit> $onFailure;
            final /* synthetic */ byte[] $bytes;
            final /* synthetic */ RevisionViewModel this$0;
            final /* synthetic */ Function1<String, Unit> $onSuccess;
            {
                this.$onFailure = $onFailure;
                this.$bytes = $bytes;
                this.this$0 = $receiver;
                this.$onSuccess = $onSuccess;
                super(2, $completion);
            }

            /*
             * Unable to fully structure code
             */
            public final Object invokeSuspend(Object $result) {
                var8_2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (this.label) {
                    case 0: {
                        ResultKt.throwOnFailure((Object)$result);
                        v0 = FirebaseAuth.getInstance().getCurrentUser();
                        v1 = uid = v0 != null ? v0.getUid() : null;
                        if (uid == null) {
                            this.$onFailure.invoke((Object)new Exception("User not logged in"));
                            return Unit.INSTANCE;
                        }
                        v2 = FirebaseStorage.getInstance().getReference();
                        Intrinsics.checkNotNullExpressionValue((Object)v2, (String)"getReference(...)");
                        storageRef = v2;
                        v3 = storageRef.child("users/" + uid + "/profile.jpg");
                        Intrinsics.checkNotNullExpressionValue((Object)v3, (String)"child(...)");
                        profileImageRef = v3;
                        v4 = profileImageRef.putBytes(this.$bytes);
                        Intrinsics.checkNotNullExpressionValue((Object)v4, (String)"putBytes(...)");
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)uid);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)storageRef);
                        this.L$2 = profileImageRef;
                        this.label = 1;
                        v5 = TasksKt.await((Task)((Task)v4), (Continuation)((Continuation)this));
                        ** if (v5 != var8_2) goto lbl28
lbl27:
                        // 1 sources

                        return var8_2;
lbl28:
                        // 1 sources

                        ** GOTO lbl37
                    }
                    case 1: {
                        profileImageRef = (StorageReference)this.L$2;
                        storageRef = (StorageReference)this.L$1;
                        uid = (String)this.L$0;
                        ResultKt.throwOnFailure((Object)$result);
                        v5 = $result;
lbl37:
                        // 2 sources

                        v6 = profileImageRef.getDownloadUrl();
                        Intrinsics.checkNotNullExpressionValue((Object)v6, (String)"getDownloadUrl(...)");
                        this.L$0 = SpillingKt.nullOutSpilledVariable((Object)uid);
                        this.L$1 = SpillingKt.nullOutSpilledVariable((Object)storageRef);
                        this.L$2 = SpillingKt.nullOutSpilledVariable((Object)profileImageRef);
                        this.label = 2;
                        v7 = TasksKt.await((Task)v6, (Continuation)((Continuation)this));
                        ** if (v7 != var8_2) goto lbl46
lbl45:
                        // 1 sources

                        return var8_2;
lbl46:
                        // 1 sources

                        ** GOTO lbl55
                    }
                    case 2: {
                        profileImageRef = (StorageReference)this.L$2;
                        storageRef = (StorageReference)this.L$1;
                        uid = (String)this.L$0;
                        try {
                            ResultKt.throwOnFailure((Object)$result);
                            v7 = $result;
lbl55:
                            // 2 sources

                            v8 = ((Uri)v7).toString();
                            Intrinsics.checkNotNullExpressionValue((Object)v8, (String)"toString(...)");
                            downloadUrl = v8;
                            current = (UserProfile)this.this$0.getCurrentUserProfile().getValue();
                            if (current != null) {
                                updated = UserProfile.copy$default(current, null, null, null, null, null, 0L, 0L, false, null, false, null, 0, 0, 0, 0, 0, null, null, null, 0, 0, false, false, 0, downloadUrl, null, 0, 0.0f, null, 0x1EFFFFFF, null);
                                this.this$0.updateUserProfile(updated);
                            }
                            this.$onSuccess.invoke((Object)downloadUrl);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            this.$onFailure.invoke((Object)e);
                        }
                        return Unit.INSTANCE;
                    }
                }
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                return (Continuation)new /* invalid duplicate definition of identical inner class */;
            }

            public final Object invoke(CoroutineScope p1, Continuation<? super Unit> p2) {
                return (this.create(p1, p2)).invokeSuspend(Unit.INSTANCE);
            }
        }), (int)3, null);
    }

    private static final void _init_$lambda$0(RevisionViewModel this$0, QuerySnapshot snapshot, FirebaseFirestoreException error) {
        if (error == null && snapshot != null) {
            this$0._unreadNotificationsCount.setValue((Object)snapshot.size());
        }
    }

    public static final /* synthetic */ int access$calculateMastery(RevisionViewModel $this, int revisions, boolean consistency) {
        return $this.calculateMastery(revisions, consistency);
    }

    public static final /* synthetic */ String access$currentDayString(RevisionViewModel $this) {
        return $this.currentDayString();
    }

    public static final /* synthetic */ long access$getStartOfDayMs(RevisionViewModel $this) {
        return $this.getStartOfDayMs();
    }

    public static final /* synthetic */ void access$showRealLocalNotification(RevisionViewModel $this, Context context, String topicId, String title, String msg, String type) {
        $this.showRealLocalNotification(context, topicId, title, msg, type);
    }

    public static final /* synthetic */ String access$formatTime(RevisionViewModel $this, long ms) {
        return $this.formatTime(ms);
    }
}
