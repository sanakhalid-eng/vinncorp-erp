import { useState, useMemo } from "react";
import {
  Search,
  ChevronUp,
  ChevronDown,
  ChevronsUpDown,
  ChevronLeft,
  ChevronRight,
  Inbox,
} from "lucide-react";
import { useIsMobile } from "../../hooks/useBreakpoint";
import { clsx } from "clsx";
import { TableRowSkeleton } from "../LoadingSkeleton";
import EmptyState from "../EmptyState";

export default function DataTable({
  columns,
  data = [],
  loading = false,
  searchable = true,
  searchPlaceholder = "Search...",
  searchFields,
  emptyMessage = "No data found",
  emptyDescription,
  emptyIcon: EmptyIcon = Inbox,
  emptyAction,
  pageSize: initialPageSize = 10,
  pageSizeOptions = [10, 25, 50, 100],
  onRowClick,
  striped = false,
  serverSide = false,
  totalPages: serverTotalPages,
  currentPage: serverCurrentPage,
  onPageChange: serverOnPageChange,
  totalRecords,
  hidePagination = false,
}) {
  const isMobile = useIsMobile();
  const [search, setSearch] = useState("");
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(initialPageSize);

  const filtered = useMemo(() => {
    if (serverSide) return data;
    if (!search.trim()) return data;
    const q = search.trim().toLowerCase();
    const fields = searchFields || columns.filter((c) => c.accessor).map((c) => c.accessor);
    return data.filter((row) =>
      fields.some((field) => {
        const val = typeof field === "function" ? field(row) : row[field];
        return val != null && String(val).toLowerCase().includes(q);
      })
    );
  }, [data, search, searchFields, columns, serverSide]);

  const sorted = useMemo(() => {
    if (!sortKey) return filtered;
    return [...filtered].sort((a, b) => {
      const col = columns.find((c) => c.accessor === sortKey);
      const getVal = col?.sortValue || ((row) => row[sortKey]);
      const aVal = getVal(a);
      const bVal = getVal(b);
      if (aVal == null && bVal == null) return 0;
      if (aVal == null) return 1;
      if (bVal == null) return -1;
      if (typeof aVal === "number" && typeof bVal === "number") {
        return sortDir === "asc" ? aVal - bVal : bVal - aVal;
      }
      const cmp = String(aVal).localeCompare(String(bVal));
      return sortDir === "asc" ? cmp : -cmp;
    });
  }, [filtered, sortKey, sortDir, columns]);

  const totalPages = serverSide
    ? serverTotalPages ?? 0
    : Math.ceil(sorted.length / pageSize);
  const currentPage = serverSide ? serverCurrentPage ?? 0 : page;
  const paginated = serverSide ? data : sorted.slice(page * pageSize, (page + 1) * pageSize);

  const handleSort = (accessor) => {
    if (!accessor) return;
    if (sortKey === accessor) {
      setSortDir(sortDir === "asc" ? "desc" : "asc");
    } else {
      setSortKey(accessor);
      setSortDir("asc");
    }
    if (!serverSide) setPage(0);
  };

  const handleSearch = (e) => {
    if (serverSide) return;
    setSearch(e.target.value);
    setPage(0);
  };

  const handlePageChange = (newPage) => {
    if (serverSide && serverOnPageChange) {
      serverOnPageChange(newPage);
    } else {
      setPage(newPage);
    }
  };

  const paginatedData = serverSide ? data : sorted;
  const safeTotalPages = serverSide
    ? serverTotalPages ?? Math.ceil((totalRecords ?? data.length) / pageSize)
    : totalPages;

  return (
    <div>
      {searchable && !serverSide && (
        <div className="mb-4 flex items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-surface-400" />
            <input
              type="text"
              placeholder={searchPlaceholder}
              value={search}
              onChange={handleSearch}
              className="w-full rounded-xl border border-surface-200 bg-white py-2.5 pl-10 pr-4 text-sm focus:border-primary-500 focus:outline-none dark:border-surface-700 dark:bg-surface-900 dark:text-surface-100"
            />
          </div>
          <select
            value={pageSize}
            onChange={(e) => { setPageSize(Number(e.target.value)); setPage(0); }}
            className="rounded-xl border border-surface-200 bg-white px-3 py-2.5 text-sm focus:border-primary-500 focus:outline-none dark:border-surface-700 dark:bg-surface-900 dark:text-surface-100"
          >
            {pageSizeOptions.map((size) => (
              <option key={size} value={size}>{size}</option>
            ))}
          </select>
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-surface-200 bg-white shadow-soft dark:border-surface-700 dark:bg-surface-900">
        {loading ? (
          isMobile ? (
            <div className="divide-y divide-surface-100 p-4 space-y-3 dark:divide-surface-800">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="animate-pulse space-y-2 rounded-xl border border-surface-100 p-4 dark:border-surface-800">
                  <div className="h-4 w-2/3 rounded bg-surface-200 dark:bg-surface-700" />
                  <div className="h-3 w-1/2 rounded bg-surface-200 dark:bg-surface-700" />
                  <div className="h-3 w-1/3 rounded bg-surface-200 dark:bg-surface-700" />
                </div>
              ))}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-surface-200 bg-surface-50 dark:border-surface-700 dark:bg-surface-800/50">
                  <tr>
                    {columns.map((col) => (
                      <th key={col.header} className="px-4 py-3 font-medium text-surface-600 dark:text-surface-400">
                        {col.header}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {Array.from({ length: 6 }).map((_, i) => (
                    <TableRowSkeleton key={i} columns={columns.length} />
                  ))}
                </tbody>
              </table>
            </div>
          )
        ) : paginatedData.length === 0 ? (
          <EmptyState
            icon={EmptyIcon}
            title={emptyMessage}
            description={emptyDescription}
            action={emptyAction}
          />
        ) : isMobile ? (
          <div className="divide-y divide-surface-100 dark:divide-surface-800">
            {paginated.map((row, idx) => (
              <div
                key={row.id || idx}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                className={clsx(
                  "space-y-2 p-4",
                  onRowClick && "cursor-pointer active:bg-surface-50 dark:active:bg-surface-800/50",
                )}
              >
                {columns
                  .filter((col) => col.header !== "Actions" || col.render)
                  .map((col) => (
                    <div key={col.header} className="flex items-start justify-between gap-3 text-sm">
                      <span className="shrink-0 text-surface-500 dark:text-surface-400">{col.header}</span>
                      <span className="text-right text-surface-900 dark:text-surface-100">
                        {col.render ? col.render(row) : row[col.accessor] ?? "-"}
                      </span>
                    </div>
                  ))}
              </div>
            ))}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-surface-200 bg-surface-50 dark:border-surface-700 dark:bg-surface-800/50">
                <tr>
                  {columns.map((col) => (
                    <th
                      key={col.header}
                      className={`px-4 py-3 font-medium text-surface-600 dark:text-surface-400 ${col.className || ""}`}
                      style={col.width ? { width: col.width } : undefined}
                    >
                      {col.accessor ? (
                        <button
                          onClick={() => handleSort(col.accessor)}
                          className="flex items-center gap-1 hover:text-surface-800 dark:hover:text-surface-200"
                        >
                          {col.header}
                          {sortKey === col.accessor ? (
                            sortDir === "asc" ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />
                          ) : (
                            <ChevronsUpDown className="h-3.5 w-3.5 text-surface-300 dark:text-surface-600" />
                          )}
                        </button>
                      ) : (
                        col.header
                      )}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className={`divide-y divide-surface-100 dark:divide-surface-800 ${striped ? "even:bg-surface-50/50 dark:even:bg-surface-800/30" : ""}`}>
                {paginated.map((row, idx) => (
                  <tr
                    key={row.id || idx}
                    onClick={onRowClick ? () => onRowClick(row) : undefined}
                    className={onRowClick ? "cursor-pointer hover:bg-surface-50 dark:hover:bg-surface-800/50" : "hover:bg-surface-50 dark:hover:bg-surface-800/50"}
                  >
                    {columns.map((col) => (
                      <td key={col.header} className={`px-4 py-3 ${col.cellClassName || ""}`}>
                        {col.render ? col.render(row) : row[col.accessor] ?? "-"}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!hidePagination && paginatedData.length > 0 && (
          <div className="flex items-center justify-between border-t border-surface-200 px-4 py-3 dark:border-surface-700">
            <p className="text-sm text-surface-500 dark:text-surface-400">
              {serverSide ? (
                totalRecords != null
                  ? `Showing ${(currentPage * pageSize) + 1}–${Math.min((currentPage + 1) * pageSize, totalRecords)} of ${totalRecords}`
                  : `Page ${currentPage + 1} of ${safeTotalPages}`
              ) : (
                `Showing ${page * pageSize + 1}–${Math.min((page + 1) * pageSize, sorted.length)} of ${sorted.length}`
              )}
            </p>
            <div className="flex items-center gap-1">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="rounded-lg p-1.5 text-surface-400 hover:bg-surface-100 disabled:opacity-30 dark:hover:bg-surface-800"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              {Array.from({ length: Math.min(5, safeTotalPages) }, (_, i) => {
                const start = Math.max(0, Math.min(currentPage - 2, safeTotalPages - 5));
                const pageNum = start + i;
                if (pageNum >= safeTotalPages) return null;
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={`rounded-lg px-3 py-1.5 text-sm font-medium ${
                      currentPage === pageNum
                        ? "bg-primary-500 text-white"
                        : "text-surface-600 hover:bg-surface-100 dark:text-surface-400 dark:hover:bg-surface-800"
                    }`}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= safeTotalPages - 1}
                className="rounded-lg p-1.5 text-surface-400 hover:bg-surface-100 disabled:opacity-30 dark:hover:bg-surface-800"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
