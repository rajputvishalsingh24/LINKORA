import * as React from "react";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "outline" | "ghost" | "secondary";
  size?: "default" | "sm" | "lg";
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className = "", variant = "default", size = "default", ...props }, ref) => {
    const base = "inline-flex items-center justify-center font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 disabled:opacity-50 disabled:pointer-events-none rounded-xl";
    const variants = {
      default: "bg-blue-600 text-white hover:bg-blue-700 shadow-sm",
      outline: "border border-slate-300 bg-transparent hover:bg-slate-100 text-slate-800",
      ghost: "hover:bg-slate-100 text-slate-700",
      secondary: "bg-slate-100 text-slate-900 hover:bg-slate-200",
    };
    const sizes = {
      default: "h-10 py-2 px-4 text-sm",
      sm: "h-9 px-3 text-xs",
      lg: "h-12 px-6 text-base",
    };

    return (
      <button
        ref={ref}
        className={`${base} ${variants[variant]} ${sizes[size]} ${className}`}
        {...props}
      />
    );
  }
);
Button.displayName = "Button";
