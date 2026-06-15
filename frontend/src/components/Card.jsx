export default function Card({ children, className }) {
  return (
    <div
      className={`bg-white rounded-xl shadow-sm p-6 transition-transform hover:shadow-md hover:-translate-y-1 ${className}`}
    >
       
      {children} 
    </div>
  );
}
