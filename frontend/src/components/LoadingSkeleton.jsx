import React from "react";

// Skeleton loader for cards

export const CardSkeleton = () => (
  <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 animate-pulse">
    <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-4"></div>
    <div className="space-y-3">
      <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
      <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-5/6"></div>
      <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-4/6"></div>
    </div>
  </div>
);

// Skeleton for table rows

export const TableRowSkeleton = ({ columns = 4 }) => (
  <tr className="animate-pulse">
    {Array(columns)
      .fill(0)
      .map((_, i) => (
        <td key={i} className="px-6 py-4">
          <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
        </td>
      ))}
  </tr>
);

// Skeleton for task cards

export const TaskCardSkeleton = () => (
  <div className="flex items-center gap-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-xl animate-pulse">
    <div className="w-3 h-3 rounded-full bg-gray-200 dark:bg-gray-600"></div>
    <div className="flex-1 space-y-2">
      <div className="h-4 bg-gray-200 dark:bg-gray-600 rounded w-3/4"></div>
      <div className="h-3 bg-gray-200 dark:bg-gray-600 rounded w-1/2"></div>
    </div>
    <div className="h-6 w-16 bg-gray-200 dark:bg-gray-600 rounded-full"></div>
  </div>
);

// Full page loading skeleton

export const PageSkeleton = () => (
  <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-100 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="animate-pulse">
        <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded w-1/3 mb-8"></div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="h-32 bg-gray-200 dark:bg-gray-700 rounded-2xl"
            ></div>
          ))}
        </div>
        <div className="h-64 bg-gray-200 dark:bg-gray-700 rounded-2xl"></div>
      </div>
    </div>
  </div>
);

// Dashboard stats skeleton

export const StatsSkeleton = () => (
  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 animate-pulse">
    {[1, 2, 3, 4].map((i) => (
      <div
        key={i}
        className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6"
      >
        <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2 mb-4"></div>
        <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-1/4"></div>
      </div>
    ))}
  </div>
);

export default {
  CardSkeleton,
  TableRowSkeleton,
  TaskCardSkeleton,
  PageSkeleton,
  StatsSkeleton,
};
