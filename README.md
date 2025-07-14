📌 Product Overview
Product Name: Continuous Backup & Continuous Recovery (CBCR)
Owner: Storage Platform / Stateful Platform Team
Goal: Ensure robust, automated, and reliable backup and restore for Uber’s stateful databases at scale—supporting business continuity, disaster recovery, compliance, and forensics.

🚨 Problems to Solve
Unreliable backup scheduling

Periodic "best-effort" backups caused resource spikes, lacking prioritization, observability, and fair network usage.

Manual, script-based restores

Recovery processes relied on stale runbooks and ad-hoc scripts, vulnerable to errors and drift.

Lack of recovery drills

No routine testing to validate whether backups could be restored reliably.

Weak RPO/RTO targets

Long RPOs (7–21 days), unknown or high RTOs (duration in days).

⚙️ Updated to ambitious SLAs: RPO of 4–24 hours, RTO of ~300 TB/hour 
Uber
+13
Uber
+13
Uber
+13
.

🎯 Objectives & Success Metrics
Adaptive Scheduling

Dynamic scheduling across databases respecting workload, network, and host capacity.

KPI: Absence of backup-induced latency or saturation events.

Automated Recovery Process

Script-free, policy-driven restore workflows.

KPI: Successful restores ≥ 99.9% of policy-scheduled drills.

Routine Restore Drills

Periodic recovery validation per database tech.

KPI: Drill coverage across database types and geo-regions.

SLAs Delivery

Maintain RPO ≤ 24 hours, RTO ≥ 300 TB/h.

KPI: Monitored backup age and volume-per-time restoration.

🏗️ System Architecture
1. Continuous Backup (Time Machine)
Central orchestrator with adaptive scheduling.

Triggers snapshot backups for MySQL, Cassandra, etcd, Zookeeper, Docstore, Schemaless.

Uploads snapshots to Uber Blobstore with rate control to avoid network spikes 
Uber
+4
Uber
+4
LinkedIn
+4
.

2. Continuous Restore
Scheduler for periodic restore drills using snapshot artifacts.

Metrics-driven validation of completeness, success, and correctness.

Generates alerts on failures or inconsistencies.

3. Unified Drivers
Backup driver – plugin-based, snapshot + upload logic per database.

Restore driver – plugin-based, download + restore logic per database.

4. Platform Components
Technology Managers/Workers – orchestrate backup/restore execution.

Uber Blobstore – scalable object storage for snapshots, with policy-driven retention across clouds 
Uber
+1
Uber
+1
.

⚙️ Features & Requirements
| Feature                             | Description                                                                                                  | Priority |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------ | -------- |
| **Adaptive Scheduling**             | Rate-limit backups; detect and defer during peak business traffic                                            | High     |
| **Backup Policies UI/API**          | Configurable scheduling and retention per database tech                                                      | High     |
| **Restore Policies & Drill Engine** | Config rules, drill frequency, success thresholds                                                            | High     |
| **Plugin Framework**                | Easily add new database tech support                                                                         | Medium   |
| **Metrics & Alerts**                | Dashboards, monitoring (RPO, RTO, backup health), alarms                                                     | High     |
| **Compliance Reporting**            | Audit logs for backups and restores                                                                          | Medium   |
| **Multi-region Support**            | Region-specific storage and restore compliance                                                               | High     |
| **Scalability**                     | Support 100 PB+ daily backup across 10k+ dbs ([LinkedIn][1], [Uber][2], [Uber][3], [Uber][4], [LinkedIn][5]) | High     |

[1]: https://www.linkedin.com/posts/uberengineering_robust-database-backup-recovery-at-uber-activity-7331348204413747200--i4h?utm_source=chatgpt.com "Database backups are critical for business continuity. - LinkedIn"
[2]: https://www.uber.com/blog/robust-database-backup-recovery-at-uber/?utm_source=chatgpt.com "Robust Database Backup Recovery at Uber | Uber Blog"
[3]: https://www.uber.com/blog/mysql-at-uber/?utm_source=chatgpt.com "MySQL At Uber | Uber Blog"
[4]: https://www.uber.com/en-BR/blog/recife/?utm_source=chatgpt.com "Recife | Latest News & Stories | Uber Blog"
[5]: https://www.linkedin.com/posts/juanestebanmrpo_robust-database-backup-recovery-at-uber-activity-7340728681897598977-wH1T?utm_source=chatgpt.com "Robust Database Backup Recovery at Uber"


🏁 Implementation Roadmap
Phase 1: MVP
Implement backup scheduler with adaptive throttling.

Plugin support for MySQL, Cassandra, etcd.

Blobstore integration, upload functionality.

Phase 2: Restore & Validation
Restore scheduler and automated restore engine.

Reporting dashboard and alerting for failures.

Initial restore drills for key databases.

Phase 3: Scale & Extend
Add support for Zookeeper, Docstore, Schemaless.

Enforce RTO/RPO metrics collection.

Add compliance/audit module.

Phase 4: Optimization & Coverage
Integrate with capacity planning tools.

Expand drills to all supported tech.

Fine-tune throttling mechanisms.

✅ Risks & Mitigations
Network contention – mitigated via adaptive throttling.

Snapshot consistency – plugin-level testing per tech.

API/Driver complexity – ensure clear schema and versioning.

Blobstore performance – monitor and adjust storage policies.

🔍 Validation Tests
Load test backup scheduling under high traffic.

Simulated failures (e.g. shard dead-nodes) and restore drills.

RPO/RTO performance validation across varying sizes.

🧩 Dependencies
Blobstore performance & durability.

Plugin development per database tech.

Monitoring infrastructure (metrics ingest, alerts).

UI/API surface in internal platform console.

🧭 Stakeholders
Storage Platform Team – execution & delivery.

Service Owners (DBAs, SREs) – testing, usage, feedback.

Security & Compliance – audit logs, restore trails.

Infrastructure/SRE – capacity, network coordination.

Business continuity – verification for DR readiness.