const getInitials = (name) => {
  if (!name) return "PM";
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .toUpperCase();
};
export default function ProjectOwnerAvatar({ ownerName, size = "md" }) {
  const sizes = {
    sm: "h-8 w-8 text-xs",
    md: "h-11 w-11 text-sm",
    lg: "h-14 w-14 text-base",
  };
  const sizeClass = sizes[size] || sizes.md;
  return (
    <div
      className={`${sizeClass} rounded-2xl bg-gradient-to-br from-primary-500 to-indigo-600 flex items-center justify-center text-white font-bold shadow-lg`}
    >
       
      {getInitials(ownerName)} 
    </div>
  );
}
