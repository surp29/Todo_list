import { DragDropContext, Draggable, Droppable } from '@hello-pangea/dnd';
import { FiCalendar, FiUser } from 'react-icons/fi';
import { PRIORITY_BADGE_CLASS, PRIORITY_LABEL, STATUS, STATUS_LABEL } from '../utils/constants';

const COLUMNS = [STATUS.PENDING, STATUS.IN_PROGRESS, STATUS.COMPLETED];

const COLUMN_HEADER_CLASS = {
  [STATUS.PENDING]: 'border-slate-200 bg-slate-50 text-slate-600',
  [STATUS.IN_PROGRESS]: 'border-blue-200 bg-blue-50 text-blue-700',
  [STATUS.COMPLETED]: 'border-emerald-200 bg-emerald-50 text-emerald-700',
};

// Dragging a card to another column calls `onStatusChange`; the backend re-checks
// permission on every request, so an Employee's board (already scoped server-side
// to their own tasks) can never affect anyone else's work.
export default function KanbanBoard({ todos, showAssignee, onStatusChange }) {
  const columns = COLUMNS.reduce((acc, status) => {
    acc[status] = todos.filter((t) => t.status === status);
    return acc;
  }, {});

  const handleDragEnd = (result) => {
    const { destination, source, draggableId } = result;
    if (!destination || destination.droppableId === source.droppableId) return;
    onStatusChange(Number(draggableId), destination.droppableId);
  };

  return (
    <DragDropContext onDragEnd={handleDragEnd}>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {COLUMNS.map((status) => (
          <div key={status} className="flex flex-col rounded-xl border border-slate-200 bg-slate-50/50">
            <div
              className={`flex items-center justify-between rounded-t-xl border-b px-4 py-3 ${COLUMN_HEADER_CLASS[status]}`}
            >
              <h3 className="text-sm font-semibold">{STATUS_LABEL[status]}</h3>
              <span className="rounded-full bg-white/70 px-2 py-0.5 text-xs font-medium">
                {columns[status].length}
              </span>
            </div>

            <Droppable droppableId={status}>
              {(provided, snapshot) => (
                <div
                  ref={provided.innerRef}
                  {...provided.droppableProps}
                  className={`min-h-[140px] flex-1 space-y-2 p-3 transition ${
                    snapshot.isDraggingOver ? 'bg-indigo-50/60' : ''
                  }`}
                >
                  {columns[status].length === 0 && !snapshot.isDraggingOver && (
                    <p className="py-6 text-center text-xs text-slate-400">Không có công việc</p>
                  )}

                  {columns[status].map((todo, index) => (
                    <Draggable key={todo.id} draggableId={String(todo.id)} index={index}>
                      {(dragProvided, dragSnapshot) => (
                        <div
                          ref={dragProvided.innerRef}
                          {...dragProvided.draggableProps}
                          {...dragProvided.dragHandleProps}
                          className={`cursor-grab rounded-lg border border-slate-200 bg-white p-3 shadow-sm active:cursor-grabbing ${
                            dragSnapshot.isDragging ? 'rotate-1 shadow-lg ring-2 ring-indigo-300' : ''
                          }`}
                        >
                          <p
                            className={`text-sm font-medium text-slate-800 ${
                              status === STATUS.COMPLETED ? 'text-slate-400 line-through' : ''
                            }`}
                          >
                            {todo.title}
                          </p>

                          <div className="mt-2 flex flex-wrap items-center gap-1.5">
                            <span
                              className={`rounded-full px-2 py-0.5 text-[11px] font-medium ${PRIORITY_BADGE_CLASS[todo.priority]}`}
                            >
                              {PRIORITY_LABEL[todo.priority]}
                            </span>
                            {showAssignee && todo.assigneeName && (
                              <span className="inline-flex items-center gap-1 rounded-full bg-indigo-50 px-2 py-0.5 text-[11px] font-medium text-indigo-700">
                                <FiUser className="h-2.5 w-2.5" />
                                {todo.assigneeName}
                              </span>
                            )}
                          </div>

                          {todo.dueDate && (
                            <p className="mt-2 flex items-center gap-1 text-[11px] text-slate-400">
                              <FiCalendar className="h-3 w-3" />
                              {todo.dueDate}
                            </p>
                          )}
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </div>
        ))}
      </div>
    </DragDropContext>
  );
}
