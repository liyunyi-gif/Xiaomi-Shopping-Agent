export function LoadingBubble() {
  return (
    <div className="mr-auto max-w-[78%] rounded-lg border border-border bg-white px-4 py-3 shadow-sm" aria-label="正在思考">
      <div className="flex items-center gap-2 text-sm text-muted">
        <span>正在理解你的需求</span>
        <span className="flex gap-1" aria-hidden="true">
          <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-ai-primary [animation-delay:-0.2s]" />
          <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-ai-primary [animation-delay:-0.1s]" />
          <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-ai-primary" />
        </span>
      </div>
    </div>
  )
}
