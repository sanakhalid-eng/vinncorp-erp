import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Mail,
  Phone,
  Building2,
  Briefcase,
  User,
  Tag,
  Edit2,
  Trash2,
  Loader2,
  StickyNote,
  Clock,
  History,
  Plus,
  X,
  Save,
  ExternalLink,
  Copy,
  CheckCircle2,
  ArrowRight,
  Users,
  Target,
} from "lucide-react";
import { toast } from "sonner";
import {
  getCustomer,
  updateCustomer,
  deleteCustomer,
  getProjectsByCustomer,
  listActivitiesByCustomer,
  getCustomerSummary,
  getEntityAuditHistory,
} from "../api/crmApi";
import { usePermission } from "../../../context/usePermission";
import { StatCard, DashboardGrid } from "../../../components/dashboard";

const TABS = [
  { id: "profile", label: "Profile", icon: User },
  { id: "projects", label: "Projects", icon: Briefcase },
  { id: "contacts", label: "Contacts", icon: Users },
  { id: "opportunities", label: "Opportunities", icon: Target },
  { id: "activities", label: "Activities", icon: Clock },
];

const INDUSTRY_OPTIONS = [
  "TECHNOLOGY", "FINANCE", "HEALTHCARE", "EDUCATION", "RETAIL",
  "MANUFACTURING", "REAL_ESTATE", "CONSULTING", "MARKETING", "OTHER",
];

function formatDate(d) {
  if (!d) return "-";
  return new Date(d).toLocaleDateString("en-US", {
    year: "numeric", month: "short", day: "numeric",
  });
}

export default function CustomerDetailPage() {
  const { customerId, workspaceSlug } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = usePermission();

  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("profile");
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({});
  const [saving, setSaving] = useState(false);

  // Projects
  const [projects, setProjects] = useState([]);
  const [projectsLoading, setProjectsLoading] = useState(false);

  // Contacts
  const [contacts, setContacts] = useState([]);
  const [contactsLoading, setContactsLoading] = useState(false);

  // Opportunities
  const [opportunities, setOpportunities] = useState([]);
  const [oppsLoading, setOppsLoading] = useState(false);

  // Activities
  const [activities, setActivities] = useState([]);
  const [activitiesLoading, setActivitiesLoading] = useState(false);

  // Summary
  const [summary, setSummary] = useState(null);
  const [summaryLoading, setSummaryLoading] = useState(false);

  const loadCustomer = useCallback(async () => {
    if (!customerId) return;
    setLoading(true);
    try {
      const data = await getCustomer(customerId);
      setCustomer(data);
      setEditForm({
        name: data.name || "",
        email: data.email || "",
        phone: data.phone || "",
        company: data.company || "",
        industry: data.industry || "",
        website: data.website || "",
        address: data.address || "",
        notes: data.notes || "",
      });
    } catch {
      toast.error("Failed to load customer");
      navigate(`/w/${workspaceSlug}/crm/customers`, { replace: true });
    } finally {
      setLoading(false);
    }
  }, [customerId, workspaceSlug, navigate]);

  const loadSummary = async () => {
    setSummaryLoading(true);
    try {
      const data = await getCustomerSummary(customerId);
      setSummary(data);
    } catch {
      // Summary is optional, don't block page load
    } finally {
      setSummaryLoading(false);
    }
  };

  useEffect(() => {
    if (customerId) loadCustomer();
  }, [customerId, loadCustomer]);

  useEffect(() => {
    if (customerId) loadSummary();
  }, [customerId]);

  useEffect(() => {
    if (!customerId) return;
    if (activeTab === "projects") loadProjects();
    if (activeTab === "contacts") loadContacts();
    if (activeTab === "opportunities") loadOpportunities();
    if (activeTab === "activities") loadActivities();
  }, [activeTab, customerId]);

  const loadProjects = async () => {
    setProjectsLoading(true);
    try {
      const data = await getProjectsByCustomer(customerId);
      setProjects(data);
    } catch {
      toast.error("Failed to load projects");
    } finally {
      setProjectsLoading(false);
    }
  };

  const loadContacts = async () => {
    setContactsLoading(true);
    try {
      // Customer contacts come from the customer entity
      setContacts(customer?.contacts || []);
    } catch {
      toast.error("Failed to load contacts");
    } finally {
      setContactsLoading(false);
    }
  };

  const loadOpportunities = async () => {
    setOppsLoading(true);
    try {
      // We need to load all opportunities for this customer
      // Use the opportunities API with customer filter
      const res = await import("../api/crmApi");
      const allOpps = await res.listOpportunities(0, 200);
      const list = Array.isArray(allOpps) ? allOpps : allOpps?.content || [];
      setOpportunities(list.filter((o) => o.customerId === Number(customerId)));
    } catch {
      toast.error("Failed to load opportunities");
    } finally {
      setOppsLoading(false);
    }
  };

  const loadActivities = async () => {
    setActivitiesLoading(true);
    try {
      const data = await listActivitiesByCustomer(customerId);
      setActivities(Array.isArray(data) ? data : []);
    } catch {
      toast.error("Failed to load activities");
    } finally {
      setActivitiesLoading(false);
    }
  };

  const setEdit = (field) => (e) => setEditForm({ ...editForm, [field]: e.target.value });

  const handleSaveProfile = async () => {
    if (!editForm.name.trim()) {
      toast.error("Customer name is required");
      return;
    }
    setSaving(true);
    try {
      await updateCustomer(customerId, editForm);
      toast.success("Customer updated");
      setEditing(false);
      loadCustomer();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to update customer");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("Delete this customer? This cannot be undone.")) return;
    try {
      await deleteCustomer(customerId);
      toast.success("Customer deleted");
      navigate(`/w/${workspaceSlug}/crm/customers`, { replace: true });
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to delete customer");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  if (!customer) return null;

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate(`/w/${workspaceSlug}/crm/customers`)}
          className="mb-4 flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-700"
        >
          <ArrowLeft className="h-4 w-4" /> Back to Customers
        </button>

        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 text-xl font-bold text-white shadow-lg shadow-emerald-500/20">
              {customer.name?.charAt(0)?.toUpperCase() || "C"}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-800">{customer.name}</h1>
              <div className="mt-1 flex items-center gap-3">
                {customer.industry && (
                  <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
                    {customer.industry}
                  </span>
                )}
                {customer.company && (
                  <span className="text-sm text-slate-500">{customer.company}</span>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {hasPermission?.("CUSTOMER_UPDATE") && (
              <button
                onClick={() => setEditing(!editing)}
                className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                <Edit2 className="h-4 w-4" /> Edit
              </button>
            )}
            {hasPermission?.("CUSTOMER_DELETE") && (
              <button
                onClick={handleDelete}
                className="flex items-center gap-2 rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-sm font-medium text-rose-600 hover:bg-rose-50"
              >
                <Trash2 className="h-4 w-4" /> Delete
              </button>
            )}
          </div>
        </div>
      </div>

      {/* ─── Customer 360 Summary ──────────────────────────────────────── */}
      {summaryLoading ? (
        <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm animate-pulse">
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-xl bg-slate-200" />
                <div className="flex-1 space-y-2">
                  <div className="h-3 w-20 rounded bg-slate-200" />
                  <div className="h-6 w-16 rounded bg-slate-200" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : summary ? (
        <div className="mb-6">
          <DashboardGrid cols={4}>
            <StatCard
              icon={Target}
              label="Total Revenue"
              value={`$${Number(summary.totalRevenue || 0).toLocaleString()}`}
              color="emerald"
            />
            <StatCard
              icon={CheckCircle2}
              label="Won Deals"
              value={summary.wonDeals || 0}
              color="blue"
            />
            <StatCard
              icon={ArrowRight}
              label="Open Opportunities"
              value={summary.openOpportunities || 0}
              color="amber"
            />
            <StatCard
              icon={Briefcase}
              label="Active Projects"
              value={summary.activeProjects || 0}
              sub={`${summary.closedProjects || 0} closed`}
              color="purple"
            />
          </DashboardGrid>
        </div>
      ) : null}

      {/* Tabs */}
      <div className="mb-6 flex gap-1 overflow-x-auto border-b border-slate-200">
        {TABS.map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 whitespace-nowrap border-b-2 px-4 py-3 text-sm font-medium transition ${
                activeTab === tab.id
                  ? "border-indigo-600 text-indigo-600"
                  : "border-transparent text-slate-500 hover:text-slate-700"
              }`}
            >
              <Icon className="h-4 w-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* ─── Profile Tab ──────────────────────────────────────────────── */}
      {activeTab === "profile" && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-800">Customer Information</h2>
              {!editing && hasPermission?.("CUSTOMER_UPDATE") && (
                <button onClick={() => setEditing(true)} className="text-sm text-indigo-600 hover:text-indigo-700">
                  Edit
                </button>
              )}
            </div>

            {editing ? (
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Name *</label>
                  <input value={editForm.name} onChange={setEdit("name")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                    <input type="email" value={editForm.email} onChange={setEdit("email")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Phone</label>
                    <input value={editForm.phone} onChange={setEdit("phone")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Company</label>
                    <input value={editForm.company} onChange={setEdit("company")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Industry</label>
                    <select value={editForm.industry} onChange={setEdit("industry")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
                      <option value="">Select...</option>
                      {INDUSTRY_OPTIONS.map((i) => <option key={i} value={i}>{i}</option>)}
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Website</label>
                  <input value={editForm.website} onChange={setEdit("website")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Address</label>
                  <textarea value={editForm.address} onChange={setEdit("address")} rows={2} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                </div>
                <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
                  <button onClick={() => { setEditing(false); setEditForm({ name: customer.name || "", email: customer.email || "", phone: customer.phone || "", company: customer.company || "", industry: customer.industry || "", website: customer.website || "", address: customer.address || "", notes: customer.notes || "" }); }} className="rounded-xl px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">
                    Cancel
                  </button>
                  <button onClick={handleSaveProfile} disabled={saving} className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50">
                    {saving && <Loader2 className="h-4 w-4 animate-spin" />}
                    Save Changes
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <InfoRow icon={Building2} label="Name" value={customer.name} />
                <InfoRow icon={Mail} label="Email" value={customer.email} copyable />
                <InfoRow icon={Phone} label="Phone" value={customer.phone} copyable />
                <InfoRow icon={Briefcase} label="Company" value={customer.company} />
                <InfoRow icon={Tag} label="Industry" value={customer.industry} />
                {customer.website && (
                  <InfoRow icon={ExternalLink} label="Website" value={customer.website} />
                )}
                {customer.address && (
                  <InfoRow icon={Building2} label="Address" value={customer.address} />
                )}
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <h3 className="mb-3 text-sm font-semibold text-slate-500 uppercase tracking-wider">Details</h3>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-slate-500">Created</span>
                  <span className="text-slate-700">{formatDate(customer.createdAt)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Last Updated</span>
                  <span className="text-slate-700">{formatDate(customer.updatedAt)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Contacts</span>
                  <span className="text-slate-700">{customer.contacts?.length || 0}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ─── Projects Tab ───────────────────────────────────────────── */}
      {activeTab === "projects" && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Linked Projects</h2>
            <span className="text-sm text-slate-400">{projects.length} project{projects.length !== 1 ? "s" : ""}</span>
          </div>

          {projectsLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : projects.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <Briefcase className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No projects linked to this customer</p>
              <p className="mt-1 text-xs text-slate-400">Won opportunities automatically create projects</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              {projects.map((proj) => (
                <div key={proj.projectId || proj.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <p className="font-semibold text-slate-800">{proj.projectName || proj.name}</p>
                        {proj.linked !== false && (
                          <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                        )}
                      </div>
                      <div className="mt-1 flex items-center gap-2">
                        {proj.projectCategory && (
                          <span className="rounded-full bg-indigo-100 px-2 py-0.5 text-xs font-medium text-indigo-700">
                            {proj.projectCategory}
                          </span>
                        )}
                        {proj.priority && (
                          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
                            {proj.priority}
                          </span>
                        )}
                      </div>
                      {proj.budget != null && (
                        <p className="mt-2 text-sm font-semibold text-indigo-600">
                          Budget: ${Number(proj.budget).toLocaleString()} {proj.currency || "USD"}
                        </p>
                      )}
                    </div>
                    <button
                      onClick={() => navigate(`/w/${workspaceSlug}/projects/${proj.projectId || proj.id}`)}
                      className="flex items-center gap-1.5 rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700"
                    >
                      View <ExternalLink className="h-3 w-3" />
                    </button>
                  </div>

                  {proj.opportunityTitle && (
                    <div className="mt-3 rounded-xl bg-slate-50 p-3 border border-slate-200">
                      <div className="flex items-center gap-2">
                        <ArrowRight className="h-3 w-3 text-slate-400" />
                        <span className="text-xs text-slate-500">From opportunity:</span>
                      </div>
                      <p className="mt-0.5 text-sm font-medium text-slate-700">{proj.opportunityTitle}</p>
                      {proj.opportunityValue != null && (
                        <p className="text-xs text-slate-500">
                          Deal value: ${Number(proj.opportunityValue).toLocaleString()}
                        </p>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ─── Contacts Tab ───────────────────────────────────────────── */}
      {activeTab === "contacts" && (
        <div>
          <h2 className="mb-4 text-lg font-semibold text-slate-800">Customer Contacts</h2>

          {contactsLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : contacts.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <Users className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No contacts linked to this customer</p>
            </div>
          ) : (
            <div className="space-y-3">
              {contacts.map((contact) => (
                <div key={contact.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                  <div className="flex items-center gap-4">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-violet-500 to-purple-600 text-sm font-bold text-white">
                      {contact.firstName?.charAt(0) || "C"}
                    </div>
                    <div className="flex-1">
                      <p className="font-medium text-slate-800">{contact.firstName} {contact.lastName}</p>
                      <div className="flex items-center gap-3 mt-0.5">
                        {contact.email && (
                          <span className="flex items-center gap-1 text-xs text-slate-500">
                            <Mail className="h-3 w-3" /> {contact.email}
                          </span>
                        )}
                        {contact.phone && (
                          <span className="flex items-center gap-1 text-xs text-slate-500">
                            <Phone className="h-3 w-3" /> {contact.phone}
                          </span>
                        )}
                      </div>
                    </div>
                    {contact.relation && (
                      <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-600">
                        {contact.relation}
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ─── Opportunities Tab ────────────────────────────────────────── */}
      {activeTab === "opportunities" && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Customer Opportunities</h2>
            <button
              onClick={() => navigate(`/w/${workspaceSlug}/crm/opportunities`)}
              className="flex items-center gap-1.5 text-sm text-indigo-600 hover:text-indigo-700"
            >
              View All <ExternalLink className="h-3.5 w-3.5" />
            </button>
          </div>

          {oppsLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : opportunities.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <Target className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No opportunities for this customer</p>
            </div>
          ) : (
            <div className="space-y-3">
              {opportunities.map((opp) => {
                const isWon = opp.stage?.isWon;
                const isLost = opp.stage?.isLost;
                return (
                  <div key={opp.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                    <div className="flex items-start justify-between">
                      <div>
                        <p className="font-semibold text-slate-800">{opp.title}</p>
                        <div className="mt-1 flex items-center gap-3">
                          {opp.value != null && (
                            <span className="text-sm font-semibold text-indigo-600">
                              ${Number(opp.value).toLocaleString()}
                            </span>
                          )}
                          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            isWon ? "bg-emerald-100 text-emerald-700" :
                            isLost ? "bg-rose-100 text-rose-700" :
                            "bg-slate-100 text-slate-600"
                          }`}>
                            {opp.stage?.name || "Unknown"}
                          </span>
                        </div>
                        {opp.notes && <p className="mt-2 text-sm text-slate-500 line-clamp-2">{opp.notes}</p>}
                      </div>
                      <button
                        onClick={() => navigate(`/w/${workspaceSlug}/crm/opportunities`)}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                      >
                        <ExternalLink className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ─── Activities Tab ───────────────────────────────────────────── */}
      {activeTab === "activities" && (
        <div>
          <h2 className="mb-4 text-lg font-semibold text-slate-800">Activity Timeline</h2>

          {activitiesLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : activities.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <Clock className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No activities logged for this customer</p>
            </div>
          ) : (
            <div className="space-y-4">
              {activities.map((act) => (
                <div key={act.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                  <div className="flex items-start gap-4">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 text-white">
                      <Clock className="h-5 w-5" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="font-medium text-slate-800">{act.subject}</p>
                      <p className="mt-0.5 text-xs text-slate-400">
                        {act.type} &middot; {formatDate(act.activityDate)}
                      </p>
                      {act.description && (
                        <p className="mt-2 text-sm text-slate-600">{act.description}</p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function InfoRow({ icon: Icon, label, value, copyable }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    if (!value) return;
    navigator.clipboard.writeText(value);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="flex items-center gap-3">
      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100">
        <Icon className="h-4 w-4 text-slate-500" />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs text-slate-400">{label}</p>
        <p className="text-sm font-medium text-slate-800">{value || "-"}</p>
      </div>
      {copyable && value && (
        <button onClick={handleCopy} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600">
          {copied ? <CheckCircle2 className="h-4 w-4 text-emerald-500" /> : <Copy className="h-4 w-4" />}
        </button>
      )}
    </div>
  );
}
