import { useCallback, useEffect, useMemo, useState } from 'react';
import * as userApi from '../api/userApi';

export default function useEmployees() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    try {
      const list = await userApi.listEmployees();
      setEmployees(list);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEmployees();
  }, [fetchEmployees]);

  const createEmployee = useCallback(
    async (data) => {
      const result = await userApi.createEmployee(data);
      await fetchEmployees();
      return result;
    },
    [fetchEmployees]
  );

  const updateEmployee = useCallback(
    async (id, data) => {
      const result = await userApi.updateEmployee(id, data);
      await fetchEmployees();
      return result;
    },
    [fetchEmployees]
  );

  const resetPassword = useCallback(async (id, newPassword) => {
    return userApi.resetEmployeePassword(id, newPassword);
  }, []);

  const removeEmployee = useCallback(
    async (id, force = false) => {
      const result = await userApi.removeEmployee(id, force);
      await fetchEmployees();
      return result;
    },
    [fetchEmployees]
  );

  // Deactivated employees can't be assigned new tasks — only feed active ones into pickers.
  const activeEmployees = useMemo(() => employees.filter((emp) => emp.active), [employees]);

  return {
    employees,
    activeEmployees,
    loading,
    createEmployee,
    updateEmployee,
    resetPassword,
    removeEmployee,
    refetch: fetchEmployees,
  };
}
