export default function DashboardGrid({ children, cols = 4 }) {
  const gridCols = {
    2: "sm:grid-cols-2",
    3: "sm:grid-cols-2 lg:grid-cols-3",
    4: "sm:grid-cols-2 lg:grid-cols-4",
    5: "sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5",
  };

  return (
    <div className={`grid grid-cols-1 gap-4 ${gridCols[cols] || gridCols[4]}`}>
      {children}
    </div>
  );
}
