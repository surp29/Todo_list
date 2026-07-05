import { useCallback, useEffect, useState } from 'react';
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

  return { employees, loading, createEmployee, refetch: fetchEmployees };
}
